# v1 - Request-Time Group Aggregate

홈 요청마다 활성 댓글을 게시글별로 그룹화하고 `MAX(created_at)`을 계산한 뒤 최신순 10개를 고르는 기준선입니다.

## 처리 흐름

```text
HomeQueryService.readRecentCommentedPosts
  -> 댓글·게시글·게시판·작성자 조인
  -> 차단·그룹 접근 권한 검사
  -> GROUP BY post_id
  -> MAX(comment.created_at)
  -> 대표값 전체 정렬
  -> LIMIT 10
```

## 핵심 코드

1. [HomeQueryService.java](./src/main/java/cluverse/home/service/HomeQueryService.java)
2. [HomeReader.java](./src/main/java/cluverse/home/service/implement/HomeReader.java)
3. [recent-commented-posts.sql](./src/main/resources/sql/recent-commented-posts.sql)
4. [HomeQueryServiceTest.java](./src/test/java/cluverse/home/service/HomeQueryServiceTest.java)

## 적용한 리팩토링

- Service는 홈 컴포넌트 크기만 결정하고 데이터 접근은 HomeReader 경계로 내렸습니다.
- 조회 SQL에 게시글·게시판·작성자 상태, 차단과 그룹 권한 조건을 함께 남겨 버전 비교의 응답 의미를 고정했습니다.

## 검증과 한계

- 테스트는 홈이 10건을 요청한다는 계약을 확인합니다.
- `LIMIT 10`은 반환 크기만 제한하며 집계·정렬 전에 방문하는 댓글과 게시글 그룹 수를 제한하지 않습니다.
- 원본 댓글만으로 결과를 복원할 수 있어 단순하지만 홈 진입마다 같은 계산을 반복합니다.
- main 소스는 `javac` 컴파일을 확인했으며 JUnit 실행 환경은 포함하지 않습니다.
