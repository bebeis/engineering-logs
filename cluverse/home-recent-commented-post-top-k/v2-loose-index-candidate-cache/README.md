# v2 - Loose Index Scan and Candidate Cache

삭제되지 않은 댓글 시각을 generated column으로 투영해 그룹 대표값 탐색을 줄이고, 전역 후보를 1분간 재사용하는 중간 단계입니다.

## 이전 버전과의 차이

- `visible_created_at`과 `(post_id, visible_created_at)` 인덱스로 Loose Index Scan 조건을 만듭니다.
- 사용자별 최종 응답이 아니라 게시글 ID와 마지막 댓글 시각으로 구성된 전역 후보만 캐시합니다.
- 차단과 그룹 접근 권한은 캐시하지 않고 요청마다 다시 읽습니다.
- 후보 캐시 범위가 부족하면 전체 집계로 폴백합니다.

## 처리 흐름

```text
HomeQueryService
  -> HomeReader
    -> 전역 후보 캐시 hit: 후보 재사용
    -> miss: Loose Index Scan으로 candidateSize + 1 조회
    -> 사용자별 접근 가능한 제목 재조회
    -> 최근순 10개 선택
    -> 부족하고 뒤 후보가 있으면 전체 집계 폴백
```

## 핵심 코드

1. [HomeReader.java](./src/main/java/cluverse/home/service/implement/HomeReader.java)
2. [HomeRecentCommentProperties.java](./src/main/java/cluverse/home/service/implement/HomeRecentCommentProperties.java)
3. [recent-comment-candidates.sql](./src/main/resources/sql/recent-comment-candidates.sql)
4. [accessible-posts.sql](./src/main/resources/sql/accessible-posts.sql)
5. [V2__add_visible_comment_index.sql](./src/main/resources/db/migration/V2__add_visible_comment_index.sql)
6. [HomeReaderTest.java](./src/test/java/cluverse/home/service/implement/HomeReaderTest.java)

## 적용한 리팩토링

- 후보 계산과 사용자별 접근 권한을 서로 다른 Reader 경계로 분리했습니다.
- 캐시 snapshot이 후보 순서와 뒤 후보 존재 여부를 함께 소유하게 했습니다.
- TTL 만료 시 동시 요청 하나만 후보를 갱신하도록 refresh를 직렬화했습니다.

## 검증과 남은 한계

- 테스트는 전역 후보 계산이 재사용되어도 사용자별 권한은 매번 검사되는지 확인합니다.
- 접근 가능한 후보가 부족하면 폴백해 후보 캐시 크기에 정확성을 의존하지 않습니다.
- `EXPLAIN`에서 `Using index for group-by`, index skip scan과 actual rows를 확인해야 합니다.
- 그룹 대표값을 최신순으로 다시 정렬하는 비용과 캐시 TTL만큼의 반영 지연은 남습니다.
- main 소스는 `javac` 컴파일을 확인했으며 JUnit 실행 환경은 포함하지 않습니다.
