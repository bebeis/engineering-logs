# v3 - Virtual Thread Bounded Processing

이미지마다 Virtual Thread를 사용하되, 실제 S3·Lambda 동시 호출 수는 공정한 세마포어로 제한합니다. 작업 표현 비용과 외부 시스템 수용량을 서로 다른 경계로 관리하는 최종 버전입니다.

## 이전 버전과의 차이

- 고정 Platform Thread 풀과 대기 큐 대신 task별 Virtual Thread를 사용합니다.
- executor 크기가 아니라 세마포어 허용량으로 외부 호출 동시성을 제한합니다.
- 모든 작업의 성공·실패 합류와 동기 완료 계약은 V2와 동일하게 유지합니다.

## 처리 흐름

```text
PostImageUploadService.upload
  -> Writer.reserve
  -> task별 Virtual Thread
       ├─ Semaphore acquire -> 이미지 1 처리 -> release
       └─ Semaphore acquire -> 이미지 2 처리 -> release
  -> CompletableFuture.allOf(...).join
  -> Writer.complete
```

## 핵심 코드

1. [PostImageUploadProcessorV3.java](./src/main/java/cluverse/post/service/implement/PostImageUploadProcessorV3.java)
2. [PostImageUploadService.java](./src/main/java/cluverse/post/service/PostImageUploadService.java)
3. [PostImageUploadWriter.java](./src/main/java/cluverse/post/service/implement/PostImageUploadWriter.java)
4. [PostImageUploadReconciler.java](./src/main/java/cluverse/post/service/implement/PostImageUploadReconciler.java)
5. [PostImageUploadProcessorV3Test.java](./src/test/java/cluverse/post/service/implement/PostImageUploadProcessorV3Test.java)

## 적용한 리팩토링

- Virtual Thread는 task 실행만 담당하고 외부 자원 보호는 별도 세마포어가 소유합니다.
- interrupt 상태를 복원하고 permit을 `finally`에서 반환해 취소가 동시성 한도를 잠식하지 않게 합니다.
- Service의 예약·보상·멱등성 계약은 실행 모델에서 분리했습니다.

## 검증과 남은 한계

- 테스트는 여러 Virtual Thread를 만들어도 실제 외부 호출 in-flight가 permit 수를 넘지 않는지 검증합니다.
- 동시 요청 64에서 V2 대비 Platform Thread 수가 151개에서 87개로 줄었고 p99는 3,581.1ms에서 3,358.1ms로 감소했습니다.
- 실제 AWS SDK 동기 호출에서 carrier pinning은 관측되지 않았지만, SDK·JDK가 바뀌면 JFR의 pinned event를 다시 확인해야 합니다.
- Virtual Thread는 외부 시스템 용량을 늘리지 않으므로 세마포어, timeout과 재시도 정책이 여전히 필요합니다.
- 핵심 계약 테스트만 포함하며 전체 애플리케이션 빌드 환경은 포함하지 않습니다.
