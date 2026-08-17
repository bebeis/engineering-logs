# v3 - Cursor Pagination

페이지 깊이에 비례하는 Offset 이동을 없앤 버전입니다. 날짜는 이동 단위가 아니라 B+Tree의 진입 앵커로만
사용하고, 이후 이동은 `(createdAt, postId)` 튜플 커서로 처리합니다.

## 이전 버전과 달라진 점

- 페이지 번호와 COUNT 쿼리를 제거했습니다.
- 날짜 검색은 `createdAt < 다음 날 00:00` 조건으로 진입 위치만 정합니다.
- 다음과 이전 이동 모두 Offset 없이 커서 인접 범위를 `size + 1`개 읽습니다.

## API 계약

- 최초 진입: 커서와 날짜 없이 요청하면 최신 글부터 조회
- 날짜 진입: `date`
- 이동: `cursorCreatedAt`, `cursorPostId`, `direction=NEXT|PREV`
- 응답: `hasNext`, `hasPrev`, 첫 글의 `prevCursor`, 마지막 글의 `nextCursor`
- 날짜와 커서는 함께 사용할 수 없고, 커서의 두 필드는 항상 쌍으로 전달

`postId` 하나만 사용하지 않은 이유는 자동 증가 ID 순서와 발행 시각 순서의 일치가 도메인 보장이 아니기
때문입니다.

## 핵심 코드 읽는 순서

1. [PostCursorRequest.java](./src/main/java/cluverse/post/service/request/PostCursorRequest.java)
   - 날짜/커서 배타성과 커서 쌍 계약
2. [PostPageQueryRepository.java](./src/main/java/cluverse/post/repository/PostPageQueryRepository.java)
   - NEXT/PREV 튜플 조건과 방향별 정렬
3. [PostListReader.java](./src/main/java/cluverse/post/service/implement/PostListReader.java)
   - ID 슬라이스와 projection 조립
4. [PostListQueryService.java](./src/main/java/cluverse/post/service/PostListQueryService.java)
   - 양방향 존재 여부와 응답 커서 계산
5. [PostListQueryServiceTest.java](./src/test/java/cluverse/post/service/PostListQueryServiceTest.java)
   - 커서 경계, 날짜 진입, PREV 의미 검증

## 쿼리 경계

```sql
-- NEXT: 더 과거
created_at < :createdAt
OR (created_at = :createdAt AND post_id < :postId)

-- PREV: 더 최신. ASC로 인접 범위를 읽고 애플리케이션에서 최신순으로 뒤집는다.
created_at > :createdAt
OR (created_at = :createdAt AND post_id > :postId)
```

동률 시 `postId`가 순서를 결정하므로 페이지 경계가 겹치지 않습니다. 고정글 조립, 검색, 작성자 정보는
커서 이동 자체와 별개라 이 버전에서 제외했습니다.

## 측정 결과

| 시나리오 | 처리량 | 평균 | p99 |
| --- | ---: | ---: | ---: |
| 최신 진입 후 NEXT 이동 | 300 TPS 이상 | 81ms | 444ms |
| 날짜 진입 후 NEXT 이동 | 300 TPS 이상 | 81ms | 469ms |
