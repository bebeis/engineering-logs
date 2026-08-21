# 홈 최근 댓글 글의 Top-K 조회 최적화

홈에는 최근 댓글이 달린 글 10개만 표시하지만, 요청마다 활성 댓글을 게시글별로 묶어 마지막 댓글 시각을 계산하면서 댓글이 늘수록 방문·정렬 비용이 커진 문제를 개선한 사례입니다. 인덱스 집계와 짧은 후보 캐시를 거쳐, 최종적으로 Top-K 정렬 키를 댓글 쓰기 시점에 게시글별 활동 행으로 투영했습니다.

전체 Cluverse 서버에서 최근 댓글 글의 세 조회 구조, 전역 후보와 사용자 권한의 캐시 경계, 최신 댓글 키의 단조 갱신·삭제 보정과 핵심 계약 테스트만 선별하고 기존 사례의 패키지·의존 방향으로 리팩토링했습니다.

## 관련 글

- [Cluverse #9: 홈 피드 개발(feat. group by 최적화)](https://velog.io/@bebeis/cluverse-9-home-feed-group-by)

## 문제

- 결과는 항상 최대 10개지만 요청마다 댓글을 `post_id`로 그룹화하고 `MAX(created_at)`을 구한 뒤 다시 정렬했습니다.
- `(post_id, created_at)` 인덱스는 그룹 안의 최신값 탐색을 도와도 모든 그룹 대표값의 최신순 정렬을 제거하지 못합니다.
- `LIMIT 10`은 대표값 계산과 정렬이 끝난 뒤 적용되므로 실제 댓글·게시글 방문 범위를 제한하지 않습니다.
- 홈 API를 컴포넌트별로 나눠도 같은 DB의 CPU·버퍼 풀·임시 정렬 공간까지 격리되지는 않습니다.
- 정렬 키를 쓰기 시점에 저장하면 동시 댓글 생성과 최신 댓글 삭제에서 원본 댓글과 투영 행의 정합성을 별도로 지켜야 합니다.

## 해결

1. V1은 요청 시 활성 댓글을 게시글별로 집계해 기준 결과를 만듭니다.
2. V2는 활성 댓글의 생성 시각을 `visible_created_at` STORED generated column으로 만들고 `(post_id, visible_created_at)` 인덱스로 Loose Index Scan을 유도합니다.
3. V2의 전역 후보 200개만 1분 캐시하고 차단 관계, 게시글·게시판·작성자 상태와 그룹 권한은 매 요청 다시 검사합니다.
4. 권한 필터 뒤 10개를 채우지 못하고 캐시 밖 후보가 남아 있으면 전체 인덱스 집계로 폴백합니다.
5. V3는 게시글마다 `(lastCommentedAt, lastCommentId)` 한 행을 저장하고 `(last_commented_at DESC, post_id DESC)` 인덱스 앞부분을 읽습니다.
6. 댓글 생성과 활동 행 갱신을 같은 트랜잭션에 두고 더 최신인 `(createdAt, commentId)`만 조건부 반영합니다.
7. 동시 최초 INSERT가 충돌하면 동일한 조건부 UPDATE를 다시 시도합니다.
8. 최신 댓글 삭제 시 활동 행을 잠그고 다음 최신 댓글로 교체하며, 남은 댓글이 없으면 활동 행을 제거합니다.
9. 댓글 상태 변경을 flush한 뒤 다음 댓글을 조회하고 삭제와 보정을 같은 트랜잭션으로 커밋합니다.

## 실패 결과

| 상황 | 결과 |
| --- | --- |
| 후보 캐시 miss | Loose Index Scan 후보를 다시 계산해 캐시 |
| 후보 캐시에서 접근 가능한 글 10개 미만 | 전체 인덱스 집계로 폴백해 정확성 유지 |
| 늦게 도착한 과거 댓글 갱신 | 조건부 UPDATE가 거부해 최신 활동 유지 |
| 같은 초의 댓글 동시 생성 | 더 큰 `commentId`를 최신으로 선택 |
| 활동 행 동시 최초 INSERT | PK 충돌 뒤 조건부 UPDATE 재시도 |
| 최신이 아닌 댓글 삭제 | 활동 행 유지 |
| 최신 댓글 삭제 | 활동 행 잠금 후 다음 댓글로 교체하거나 행 제거 |
| 댓글·활동 갱신 중 실패 | 같은 DB 트랜잭션 전체 롤백 |

## 버전별 코드

- [`v1-request-time-group-aggregate/`](./v1-request-time-group-aggregate/): 요청마다 `GROUP BY post_id + MAX(created_at)` 계산
- [`v2-loose-index-candidate-cache/`](./v2-loose-index-candidate-cache/): generated column 기반 Loose Index Scan과 전역 후보 캐시
- [`v3-write-time-activity-projection/`](./v3-write-time-activity-projection/): 댓글 변경 시 게시글별 최신 댓글 정렬 키 투영

각 버전은 `HomeQueryService`가 홈 컴포넌트의 10건 조회 계약을 소유하고 실제 읽기는 `cluverse.home.service.implement` 경계로 내립니다. V3의 댓글 쓰기는 `CommentProcessor`가 댓글 원본과 활동 투영을 한 생명주기로 다루며 Service는 Repository를 직접 참조하지 않습니다.

실제 JDBC RowMapper·QueryDSL, 차단·그룹 권한 조회 구현, Controller와 응답 DTO, Caffeine·Micrometer 설정, 공공 API와 다른 홈 컴포넌트, 부하·정합성 스크립트는 생략했습니다. 각 버전 main 코드는 독립적으로 컴파일할 수 있지만 테스트 빌드 설정과 JUnit 의존성은 포함하지 않습니다.

## 결과

- V1은 댓글 수와 댓글이 있는 게시글 수가 늘면 그룹 대표값 계산과 Top-K 정렬 비용이 함께 증가합니다.
- V2는 댓글 전체 방문을 게시글 그룹 대표값 수준으로 줄이고 warm cache에서 반복 집계를 피하지만, 캐시 갱신 시 그룹 대표값 정렬과 최대 1분 반영 지연이 남습니다.
- V3는 요청 시 집계를 제거하고 활동 인덱스의 앞부분에서 접근 가능한 10건을 찾는 대신 댓글 생성·삭제 경로에 투영 유지 비용을 추가합니다.
- 세 단계는 같은 fixture에서 API p95·p99, `EXPLAIN ANALYZE` actual rows·sort·temporary, 캐시 적중·폴백, 댓글 쓰기 지연과 원본·투영 불일치를 함께 비교해야 합니다.
- 글에는 완료된 측정 수치가 공개되지 않아 구조와 검증 기준만 기록했습니다.
