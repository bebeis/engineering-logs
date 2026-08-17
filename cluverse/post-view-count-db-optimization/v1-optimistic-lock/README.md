# v1 - Optimistic Lock

`@Version`으로 충돌을 감지하고, 충돌한 조회수 증가를 최대 10회까지 새 트랜잭션으로 재시도하는 비교 구현입니다.

## 요청 흐름

```text
PostViewCountCommandService.increaseViewCount
  -> PostMetaWriter.increaseViewCountOptimistic
    -> REQUIRES_NEW 트랜잭션 시작
    -> PostViewCountOptimistic 조회 또는 생성
    -> PostViewCountOptimistic.increase
    -> version 조건 UPDATE와 flush
    -> 충돌이면 잠시 대기한 뒤 새 트랜잭션으로 재시도
```

## 적용한 리팩토링

- 세 버전의 공개 유스케이스를 `PostViewCountCommandService.increaseViewCount`로 통일했습니다.
- 조회수 저장 흐름은 원본과 동일하게 `cluverse.meta.service.implement.PostMetaWriter`가 소유합니다.
- 전체 `PostMetaWriter`에서 낙관적 락 비교에 필요한 의존성과 메서드만 남겼습니다.
- 공통 시간 엔티티, 게시글 존재 검증, Controller와 응답 래퍼는 전략 비교와 무관해 생략했습니다.

## 핵심 코드

1. [PostViewCountCommandService.java](./src/main/java/cluverse/post/service/PostViewCountCommandService.java)
   - 낙관적 락 쓰기 전략으로 위임하는 유스케이스
2. [PostMetaWriter.java](./src/main/java/cluverse/meta/service/implement/PostMetaWriter.java)
   - 새 트랜잭션 재시도와 재시도 소진 정책
3. [PostViewCountOptimistic.java](./src/main/java/cluverse/meta/domain/PostViewCountOptimistic.java)
   - `@Version`을 가진 조회수 엔티티
4. [PostViewCountOptimisticRepository.java](./src/main/java/cluverse/meta/repository/PostViewCountOptimisticRepository.java)
   - 버전 조건 UPDATE를 발생시키는 JPA Repository
5. [PostMetaWriterTest.java](./src/test/java/cluverse/meta/service/implement/PostMetaWriterTest.java)
   - 조회수 레코드 생성과 증가 계약
6. [PostViewCountCommandServiceTest.java](./src/test/java/cluverse/post/service/PostViewCountCommandServiceTest.java)
   - 낙관적 락 전략 위임 계약

## 검증과 한계

- 원문의 100 RPS 측정에서는 DB 락 대기 대신 충돌 요청의 반복 재시도로 응답 시간이 증가했습니다.
- 충돌이 잦은 단일 카운터에서 재시도도 UPDATE와 커밋 비용을 다시 지불하므로 다음 버전의 비교 대상이 됩니다.
- 핵심 계약 테스트는 포함하지만 이 저장소에는 전체 애플리케이션 빌드 설정이 없어 실행하지 않습니다.
