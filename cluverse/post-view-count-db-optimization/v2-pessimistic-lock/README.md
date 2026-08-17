# v2 - Pessimistic Lock

서버 스케일아웃 뒤 동시성 제어 지점을 DB로 옮긴 버전입니다. `SELECT FOR UPDATE`로 조회수 레코드를 잠그고 JPA 변경 감지로 값을 갱신합니다.

## 이전 버전과의 차이

- JVM별로 분리된 모니터 락을 제거합니다.
- 모든 애플리케이션 인스턴스가 공유하는 DB 레코드 락을 사용합니다.
- 대신 락을 잡은 뒤 엔티티를 애플리케이션으로 읽고 다시 UPDATE할 때까지 다음 요청이 기다립니다.

## 요청 흐름

```text
PostViewCountCommandService.increaseViewCount  # 트랜잭션 시작
  -> PostMetaWriter.increaseViewCountPessimistic
    -> PostViewCountRepository.findByPostIdForUpdate  # SELECT ... FOR UPDATE
    -> PostViewCount.increase
    -> dirty checking UPDATE
  -> COMMIT 후 DB 레코드 락 해제
```

## 적용한 리팩토링

- Service의 공개 계약은 v1과 동일하게 유지하고 Writer의 증가 전략만 교체합니다.
- 비관적 락 쓰기 흐름은 `cluverse.meta.service.implement.PostMetaWriter`가 소유합니다.
- 저장 엔티티와 Repository는 `cluverse.meta`에 두어 조회수 저장 모델의 수명을 게시글 유스케이스와 분리합니다.
- 전체 `PostMetaWriter`에서 비관적 락 비교에 필요한 의존성과 메서드만 남깁니다.
- 테스트는 실제로 바뀐 Writer와 Repository 사이의 계약을 검증합니다.

## 핵심 코드

1. [PostViewCountCommandService.java](./src/main/java/cluverse/post/service/PostViewCountCommandService.java)
   - 버전 간 동일한 조회수 증가 유스케이스
2. [PostMetaWriter.java](./src/main/java/cluverse/meta/service/implement/PostMetaWriter.java)
   - 잠긴 엔티티를 변경하고 커밋까지 트랜잭션 유지
3. [PostViewCountRepository.java](./src/main/java/cluverse/meta/repository/PostViewCountRepository.java)
   - `PESSIMISTIC_WRITE`로 조회수 레코드를 선점
4. [PostViewCount.java](./src/main/java/cluverse/meta/domain/PostViewCount.java)
   - 변경 감지 대상 조회수 카운터
5. [PostMetaWriterTest.java](./src/test/java/cluverse/meta/service/implement/PostMetaWriterTest.java)
   - 비관적 락 조회 결과를 한 번 증가시키는 계약
6. [PostViewCountCommandServiceTest.java](./src/test/java/cluverse/post/service/PostViewCountCommandServiceTest.java)
   - 비관적 락 전략 위임 계약

## 검증과 한계

- 원문의 부하 테스트에서 100 RPS 평균 응답 시간은 약 127ms였습니다.
- 300 RPS를 넣어도 처리량은 약 200 TPS에서 멈췄고, 응답 시간 중앙값은 약 470ms까지 증가했습니다.
- 핵심 계약 테스트는 포함하지만 전체 빌드와 k6 시나리오는 없어 이 저장소에서는 실행하지 않습니다.
