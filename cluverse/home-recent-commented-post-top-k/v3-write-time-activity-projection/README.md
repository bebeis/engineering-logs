# v3 - Write-Time Activity Projection

원본 댓글에 없던 게시글별 최신 댓글 정렬 키를 `post_comment_activity`에 한 행씩 저장하고, 홈 조회에서는 최신순 인덱스 앞부분만 읽습니다.

## 이전 버전과의 차이

- 요청 시 그룹 집계와 후보 캐시를 제거합니다.
- 댓글 생성·삭제 트랜잭션에서 활동 투영을 함께 갱신합니다.
- `(lastCommentedAt, lastCommentId)`가 더 최신일 때만 활동 행을 바꿉니다.
- 최신 댓글 삭제 시 다음 최신 댓글로 보정하거나 활동 행을 제거합니다.

## 쓰기와 조회 흐름

```text
댓글 생성
  -> Comment INSERT
  -> 게시글 댓글 수 갱신
  -> post_comment_activity 조건부 UPSERT
  -> COMMIT

최신 댓글 삭제
  -> 댓글·활동 행 잠금
  -> 댓글 삭제 상태 flush
  -> 다음 최신 활성 댓글 조회
  -> 활동 교체 또는 삭제
  -> COMMIT

홈 조회
  -> idx_post_comment_activity_latest 앞부분 탐색
  -> 접근 권한 조건을 통과한 10건 반환
```

## 핵심 코드

1. [CommentProcessor.java](./src/main/java/cluverse/comment/service/implement/CommentProcessor.java)
2. [PostCommentActivityWriter.java](./src/main/java/cluverse/comment/service/implement/PostCommentActivityWriter.java)
3. [LatestCommentKey.java](./src/main/java/cluverse/comment/domain/LatestCommentKey.java)
4. [PostCommentActivity.java](./src/main/java/cluverse/comment/domain/PostCommentActivity.java)
5. [activity-upsert.sql](./src/main/resources/sql/activity-upsert.sql)
6. [recent-commented-posts.sql](./src/main/resources/sql/recent-commented-posts.sql)
7. [V3__create_post_comment_activity.sql](./src/main/resources/db/migration/V3__create_post_comment_activity.sql)
8. [LatestCommentKeyTest.java](./src/test/java/cluverse/comment/domain/LatestCommentKeyTest.java)
9. [PostCommentActivityWriterTest.java](./src/test/java/cluverse/comment/service/implement/PostCommentActivityWriterTest.java)

## 적용한 리팩토링

- 최신 여부를 시각과 ID의 순서쌍인 `LatestCommentKey`로 표현해 비교 규칙을 순수 객체에 모았습니다.
- 댓글 원본과 활동 투영은 CommentProcessor의 같은 쓰기 생명주기에 둡니다.
- 최신 댓글 삭제 보정은 활동 Writer가 행 잠금, flush와 다음 댓글 선택 순서를 소유합니다.
- 홈 Service는 투영 유지 방법을 알지 않고 HomeReader만 참조합니다.

## 검증과 남은 한계

- 테스트는 생성 시각 우선·동일 초 ID tie-breaker와 최신 댓글 삭제 후 교체·제거를 검증합니다.
- 배포 시 `ROW_NUMBER()`로 기존 댓글의 최신 행을 한 번 백필하고 원본과 투영의 일치 여부를 확인해야 합니다.
- 조회 비용을 쓰기로 옮겼으므로 댓글 작성 p95·p99와 동시 UPSERT 실패율을 함께 측정해야 합니다.
- 옵티마이저가 활동 인덱스를 선두로 읽고 sort 없이 10건을 채우는지 `EXPLAIN ANALYZE`로 확인해야 합니다.
- main 소스는 `javac` 컴파일을 확인했으며 JUnit 실행 환경은 포함하지 않습니다.
