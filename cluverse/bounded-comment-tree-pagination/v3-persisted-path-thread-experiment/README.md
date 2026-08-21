# v3 - Persisted Path Thread Experiment

재귀 시점에 만들던 정렬 경로를 댓글 행의 `path`로 저장하고 `(post_id, path)` 인덱스 범위에서 스레드 페이지를 읽는 비교안입니다.

## 이전 버전과의 차이

- 스레드 조회에서 재귀 CTE와 매 요청의 `sort_path` 생성을 제거합니다.
- 댓글 INSERT 후 생성된 ID로 `parent.path + created_at·comment_id` 경로를 만들고 UPDATE합니다.
- path의 ASCII binary 정렬과 `(post_id, path)` 인덱스를 읽기 계약에 추가합니다.

## 쓰기와 조회 흐름

```text
댓글 작성
  -> 부모 FOR UPDATE
  -> 댓글 INSERT, 생성 ID 확인
  -> 부모 path + 정렬 조각 생성
  -> path UPDATE
  -> COMMIT

스레드 조회
  -> root path prefix
  -> path > afterPath
  -> ORDER BY path LIMIT limit + 1
```

## 핵심 코드

1. [Comment.java](./src/main/java/cluverse/comment/domain/Comment.java)
2. [CommentPathWriteProcessor.java](./src/main/java/cluverse/comment/service/implement/CommentPathWriteProcessor.java)
3. [CommentQueryService.java](./src/main/java/cluverse/comment/service/CommentQueryService.java)
4. [path-thread-page.sql](./src/main/resources/sql/path-thread-page.sql)
5. [V3__add_comment_path.sql](./src/main/resources/db/migration/V3__add_comment_path.sql)
6. [CommentPathTest.java](./src/test/java/cluverse/comment/domain/CommentPathTest.java)

## 적용한 리팩토링

- path 생성 규칙과 최대 길이 불변식을 댓글 도메인에 모았습니다.
- 저장소는 INSERT와 path UPDATE를 노출하고 Processor가 한 쓰기 생명주기로 묶습니다.
- V2의 삭제 잠금과 표시 정책은 그대로 유지하고, 읽기·쓰기 교환에 직접 관련된 코드만 남겼습니다.

## 검증과 남은 한계

- 테스트는 부모 prefix 누적, 동일 시각 형제의 ID 순서와 최대 path 길이를 검증합니다.
- 기존 데이터는 nullable 컬럼 추가, 신규 쓰기 적용, depth 순서의 작은 배치 백필, 불변식 확인, 인덱스 생성, `NOT NULL` 전환 순서가 필요합니다.
- AUTO_INCREMENT 때문에 댓글 작성에 INSERT와 UPDATE가 필요하며 인덱스 크기와 버퍼 풀 점유도 증가합니다.
- 재귀 CTE보다 유리한지는 결과 동등성과 읽기·쓰기 부하 측정 전에는 확정할 수 없습니다.
- main 소스는 `javac` 컴파일을 확인했으며 JUnit 실행 환경은 포함하지 않습니다.
