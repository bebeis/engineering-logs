# v1 - Sequential Lambda Processing

요청 스레드가 각 이미지의 staging 업로드와 Lambda 동기 호출을 하나씩 끝낸 뒤 다음 이미지를 처리하는 비교 기준선입니다.

## 처리 흐름

```text
PostImageUploadService.upload
  -> Writer.reserve                 # 짧은 DB 트랜잭션, PENDING
  -> 이미지 1: S3 staging -> Lambda -> 출력 검증
  -> 이미지 2: S3 staging -> Lambda -> 출력 검증
  -> Writer.complete                # 짧은 DB 트랜잭션, COMPLETED
  -> staging 삭제
```

## 핵심 코드

1. [PostImageUploadService.java](./src/main/java/cluverse/post/service/PostImageUploadService.java)
2. [PostImageUploadProcessorV1.java](./src/main/java/cluverse/post/service/implement/PostImageUploadProcessorV1.java)
3. [PostImageUploadWriter.java](./src/main/java/cluverse/post/service/implement/PostImageUploadWriter.java)
4. [PostImageUploadReconciler.java](./src/main/java/cluverse/post/service/implement/PostImageUploadReconciler.java)
5. [PostImageUploadProcessorV1Test.java](./src/test/java/cluverse/post/service/implement/PostImageUploadProcessorV1Test.java)

## 적용한 리팩토링

- Service는 외부 I/O 순서를 조정하지만 Repository에 직접 접근하지 않습니다.
- 저장소와 Lambda 호출을 client 인터페이스 뒤로 격리했습니다.
- 처리 완료는 Lambda 응답만 믿지 않고 예정한 두 출력 객체의 크기를 다시 확인합니다.
- timeout과 일반 실패의 보상 시점을 구분하고 `COMPENSATING` 선점으로 삭제 경합을 차단합니다.

## 한계

- 이미지가 늘수록 외부 호출 대기가 직렬로 누적됩니다.
- 요청 스레드가 모든 Lambda 응답을 기다리지만, 이는 업로드 응답 시점에 이미지 사용 가능 상태를 보장하는 동기 완료 계약을 위한 선택입니다.
- 핵심 계약 테스트만 포함하며 전체 애플리케이션 빌드 환경은 포함하지 않습니다.
