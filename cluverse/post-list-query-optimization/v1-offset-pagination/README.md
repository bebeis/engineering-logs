# v1 - Offset Pagination

인덱스가 있지만 깊은 페이지에서 느려지는 최초 구현입니다. OFFSET에 도달하기 전에 projection과
조인이 적용되고, 페이지 응답을 만들기 위해 정확한 전체 `COUNT(*)`를 별도로 실행합니다.

## 요청 흐름

```text
PostListController
  -> PostListQueryService
    -> PostListReader
      -> findSummariesWithOffset  # projection JOIN + OFFSET
      -> countActivePosts         # 전체 COUNT
```

## 핵심 코드

1. [PostListQueryRepository.java](./src/main/java/cluverse/post/repository/PostListQueryRepository.java)
   - 버릴 행에도 projection과 조인이 적용되는 기준선 쿼리
2. [PostListReader.java](./src/main/java/cluverse/post/service/implement/PostListReader.java)
   - 목록과 전체 개수를 함께 읽는 Implement
3. [PostListQueryService.java](./src/main/java/cluverse/post/service/PostListQueryService.java)
   - 전체 개수로 페이지 블록을 계산하는 흐름
4. [PostListQueryServiceTest.java](./src/test/java/cluverse/post/service/PostListQueryServiceTest.java)
   - 다음 블록과 실제 마지막 페이지의 응답 계약
5. [post-list-index.sql](./src/main/resources/post-list-index.sql)
   - 이미 존재했지만 Offset의 선형 이동 비용을 없애지는 못한 인덱스

## 병목

- B+Tree가 첫 위치는 빠르게 찾아도 Offset만큼의 인덱스 엔트리를 읽고 버립니다.
- 이 구현은 버리는 동안에도 클러스터드 인덱스 조회와 썸네일·카운터·작성자 조인을 수행합니다.
- `COUNT(*)`는 MVCC 스냅샷에서 보이는 행을 확인해야 하므로 조건 범위를 다시 스캔합니다.

QueryDSL Q 타입과 조인 대상 Entity는 생성·주변 코드이므로 포함하지 않았습니다. 이 버전은 독립 빌드보다
문제 재현 쿼리와 호출 흐름을 보존하는 데 목적이 있습니다.
