# v2 - Sibling Cursor and Recursive Thread

기본 목록은 한 부모의 직계 자식만 복합 커서로 읽고, 여러 depth의 문맥은 시작 댓글 하나를 갖는 재귀 CTE 스레드로 분리합니다. 답글 작성과 삭제는 같은 댓글 행 잠금으로 직렬화합니다.

## 이전 버전과의 차이

- 루트 페이지에 전체 자손을 붙이지 않고 부모별 직계 자식 페이지를 독립적으로 조회합니다.
- Offset을 `(created_at, comment_id)` 커서로 바꾸고 전체 COUNT 대신 `limit + 1`로 `hasNext`를 판단합니다.
- 깊은 대화만 별도 스레드 조회로 제한합니다.
- 답글 INSERT와 leaf DELETE가 같은 부모·대상 행을 먼저 잠급니다.

## 요청 흐름

```text
기본 목록·답글 펼치기
  -> CommentQueryService.readChildren
  -> 직계 자식 ID limit + 1 조회
  -> 제한된 ID의 화면 정보 일괄 조립

깊은 문맥
  -> CommentQueryService.readThread
  -> 시작 댓글 anchor의 재귀 CTE
  -> 깊이 우선 sort_path 커서로 limit + 1 조회

답글 작성·삭제
  -> 부모 또는 대상 SELECT FOR UPDATE
  -> ACTIVE·depth 또는 자식 존재 확인
  -> INSERT / soft delete / physical delete
```

## 핵심 코드

1. [CommentQueryService.java](./src/main/java/cluverse/comment/service/CommentQueryService.java)
2. [CommentCursor.java](./src/main/java/cluverse/comment/domain/CommentCursor.java)
3. [direct-children-page.sql](./src/main/resources/sql/direct-children-page.sql)
4. [recursive-thread-page.sql](./src/main/resources/sql/recursive-thread-page.sql)
5. [CommentWriteProcessor.java](./src/main/java/cluverse/comment/service/implement/CommentWriteProcessor.java)
6. [comment-indexes.sql](./src/main/resources/sql/comment-indexes.sql)
7. [CommentQueryServiceTest.java](./src/test/java/cluverse/comment/service/CommentQueryServiceTest.java)
8. [CommentWriteProcessorTest.java](./src/test/java/cluverse/comment/service/implement/CommentWriteProcessorTest.java)

## 적용한 리팩토링

- 직계 자식 페이지와 스레드 페이지를 서로 다른 요청 계약으로 분리했습니다.
- 커서가 정렬 위치뿐 아니라 최초 조회 시각과 최대 ID를 소유해 페이지 집합을 고정합니다.
- 쓰기 Processor가 잠금 순서와 삭제 결과를 함께 소유하도록 했습니다.
- 삭제 상태의 부모를 조회 행에서 제거하지 않아 자식 연결을 보존합니다.

## 검증과 남은 한계

- 테스트는 `limit + 1`, 동일 시각 ID tie-breaker와 답글·삭제의 공통 잠금 순서를 검증합니다.
- 직계 자식 조회는 `(post_id, parent_id, created_at, comment_id)` 인덱스로 요청 비용을 제한합니다.
- 재귀 CTE의 최종 LIMIT은 논리적 응답 상한이며 실제 방문 행 수는 `EXPLAIN ANALYZE`로 별도 확인해야 합니다.
- main 소스는 `javac` 컴파일을 확인했으며 JUnit 실행 환경은 포함하지 않습니다.
