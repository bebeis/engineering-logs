# v2 - Platform Thread Parallel Processing

이미지별 staging 업로드와 Lambda 동기 호출을 `CompletableFuture`로 제출하고, 크기와 큐가 제한된 Platform Thread 풀에서 겹쳐 실행합니다.

## 이전 버전과의 차이

- 한 요청의 이미지 처리를 순차 반복하지 않고 고정 스레드 풀에 병렬 제출합니다.
- 모든 작업이 합류한 뒤에만 DB 완료 트랜잭션을 실행합니다.
- 일부 제출이 거절되더라도 이미 제출된 작업의 종료를 기다린 뒤 실패를 호출자에게 전파합니다.

## 처리 흐름

```text
PostImageUploadService.upload
  -> Writer.reserve
  -> fixed Platform Thread executor
       ├─ 이미지 1: staging -> Lambda -> 검증
       └─ 이미지 2: staging -> Lambda -> 검증
  -> CompletableFuture.allOf(...).join
  -> Writer.complete
```

## 핵심 코드

1. [PostImageUploadProcessorV2.java](./src/main/java/cluverse/post/service/implement/PostImageUploadProcessorV2.java)
2. [PostImageUploadService.java](./src/main/java/cluverse/post/service/PostImageUploadService.java)
3. [PostImageUploadWriter.java](./src/main/java/cluverse/post/service/implement/PostImageUploadWriter.java)
4. [PostImageUploadReconciler.java](./src/main/java/cluverse/post/service/implement/PostImageUploadReconciler.java)
5. [PostImageUploadProcessorV2Test.java](./src/test/java/cluverse/post/service/implement/PostImageUploadProcessorV2Test.java)

## 적용한 리팩토링

- 비동기 작업에 JPA 엔티티를 전달하지 않고 불변 key 명령만 전달합니다.
- task 실패와 executor 제출 거절을 구분하되 둘 다 요청 실패로 합류시킵니다.
- DB 트랜잭션은 외부 호출 전후의 예약·완료 Writer에만 둡니다.

## 검증과 한계

- barrier 기반 테스트로 두 외부 호출이 실제로 겹치는지 검증합니다.
- 동시 요청 64에서 V1 대비 처리량이 약 2.03배로 늘고 p99가 약 56% 감소했습니다.
- 부하가 증가하면 풀의 Platform Thread와 대기 큐가 함께 늘어납니다. 같은 조건에서 Platform Thread 151개와 최대 큐 22개를 관측했습니다.
- 핵심 계약 테스트만 포함하며 전체 애플리케이션 빌드 환경은 포함하지 않습니다.
