# v1 - Transactional Place Verification

외부 장소 재검색부터 Place Upsert, Post와 PostMeta 저장, PostPlace 연결까지 하나의 트랜잭션에서 실행한 기준선입니다.

## 요청 흐름

```text
LocalMapPostWriteService.create
  -> LocalMapPostWriteProcessor.create  # 트랜잭션 시작
    -> PlaceSelectionResolver.resolve   # 외부 API 대기
    -> PlaceWriter.upsertAll
    -> PostWriter.create
    -> PostMetaWriter.createViewCount
    -> Post.addPlace
  -> COMMIT
```

## 적용한 리팩토링

- 외부 검증을 포함한 트랜잭션 경계를 `LocalMapPostWriteProcessor` 한 곳에서 보이도록 정리했습니다.
- 장소 provider 호출은 Resolver, Place 저장은 Writer와 Repository가 소유합니다.
- 게시판·회원 검증과 캠퍼스 추천 계산은 비교의 핵심이 아니므로 제외했습니다.

## 핵심 코드

1. [LocalMapPostWriteService.java](./src/main/java/cluverse/post/service/LocalMapPostWriteService.java)
2. [LocalMapPostWriteProcessor.java](./src/main/java/cluverse/post/service/implement/LocalMapPostWriteProcessor.java)
3. [PlaceSelectionResolver.java](./src/main/java/cluverse/place/service/implement/PlaceSelectionResolver.java)
4. [PlaceWriter.java](./src/main/java/cluverse/place/service/implement/PlaceWriter.java)
5. [LocalMapPostWriteProcessorTest.java](./src/test/java/cluverse/post/service/implement/LocalMapPostWriteProcessorTest.java)

## 검증과 한계

- 외부 API가 실패하면 게시글 전체가 롤백됩니다.
- 외부 대기 동안 DB 커넥션을 보유해 풀 크기가 처리량 상한이 됩니다.
- 15 RPS에서는 SLO를 통과했지만 16 RPS에서 p99 1.77초와 dropped iteration 8건을 기록했습니다.
- 핵심 계약 테스트는 포함하지만 공통 빌드 설정이 없어 실행하지 않습니다.
