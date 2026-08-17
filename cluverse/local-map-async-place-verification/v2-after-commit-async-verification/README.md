# v2 - AFTER_COMMIT Async Verification

Post와 PostMeta를 먼저 커밋하고, 외부 장소 검증은 `AFTER_COMMIT + @Async` 이벤트에서 수행한 뒤 별도의 짧은 트랜잭션으로 장소를 연결합니다.

## 이전 버전과의 차이

- 게시글 트랜잭션에서 외부 API 호출과 Place·PostPlace 저장을 제거합니다.
- 롤백된 게시글에는 장소 검증 이벤트가 전달되지 않습니다.
- 외부 검증과 장소 연결 실패가 이미 커밋된 게시글로 전파되지 않습니다.

## 요청과 완료 흐름

```text
LocalMapPostWriteService.create
  -> LocalMapPostWriteProcessor.create  # 첫 번째 트랜잭션
    -> PostWriter.create
    -> PostMetaWriter.createViewCount
    -> PostPlaceVerificationRequested 발행
  -> COMMIT 후 게시글 ID 반환

AsyncPostPlaceVerificationHandler.verify  # AFTER_COMMIT + 전용 executor
  -> PlaceSelectionResolver.resolve       # 트랜잭션 없는 외부 API 대기
  -> PostPlaceCompletionProcessor.complete  # 두 번째 트랜잭션
    -> PlaceWriter.upsertAll
    -> Post.addPlace
```

## 적용한 리팩토링

- 게시글 저장, 외부 검증, 장소 연결을 실패 결과와 트랜잭션 수명에 따라 세 컴포넌트로 분리했습니다.
- 이벤트는 불변 목록을 소유하고 Handler는 외부 실패를 게시글 트랜잭션 밖에서 종료합니다.
- 장소 Upsert와 PostPlace 연결만 Completion Processor의 트랜잭션에 남겼습니다.
- executor 설정을 함께 두어 비동기 큐 용량도 처리량 경계임을 드러냈습니다.

## 핵심 코드

1. [LocalMapPostWriteProcessor.java](./src/main/java/cluverse/post/service/implement/LocalMapPostWriteProcessor.java)
2. [PostPlaceVerificationRequested.java](./src/main/java/cluverse/post/service/implement/PostPlaceVerificationRequested.java)
3. [AsyncPostPlaceVerificationHandler.java](./src/main/java/cluverse/post/service/implement/AsyncPostPlaceVerificationHandler.java)
4. [PostPlaceCompletionProcessor.java](./src/main/java/cluverse/post/service/implement/PostPlaceCompletionProcessor.java)
5. [LocalMapAsyncConfig.java](./src/main/java/cluverse/place/config/LocalMapAsyncConfig.java)
6. [LocalMapPostWriteProcessorTest.java](./src/test/java/cluverse/post/service/implement/LocalMapPostWriteProcessorTest.java)
7. [AsyncPostPlaceVerificationHandlerTest.java](./src/test/java/cluverse/post/service/implement/AsyncPostPlaceVerificationHandlerTest.java)
8. [LocalMapPostWriteServiceTest.java](./src/test/java/cluverse/post/service/LocalMapPostWriteServiceTest.java)

## 검증과 남은 한계

- 103 RPS에서 API p99 43.98ms, 성공률 100%, dropped 0과 provider 호출 완료를 함께 확인했습니다.
- provider 호출 자체는 평균 300.19ms, p99 357.02ms로 여전히 느리지만 게시글 트랜잭션 밖에서 실행됩니다.
- 104 RPS에서는 API가 성공해도 비동기 작업 13건이 제한 시간 안에 provider에 도달하지 못했습니다.
- 인메모리 이벤트와 executor는 프로세스 종료, 큐 포화와 provider 장애 후 전달을 보장하지 않습니다. 최종 전달이 요구되면 Transactional Outbox가 후속 단계입니다.
- 핵심 계약 테스트는 포함하지만 전체 애플리케이션 빌드와 executor 통합 환경이 없어 실행하지 않습니다.
