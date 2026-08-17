# v4 - Total Counter

MySQL 누적값을 필요할 때 Redis에 적재하고, Redis 전체 카운터를 화면 표시 원천으로 사용하는 최종 구조입니다. MySQL은 `GREATEST` 기반 내구성 체크포인트로 남습니다.

## 요청과 백그라운드 흐름

```text
PostViewCountCommandService.increaseViewCount
  -> TotalViewCountCounter.count
    -> count_total.lua
    -> REINITIALIZE이면 ViewCountInitializer.ensureInitialized
    -> Redis 장애이면 LocalViewCountFallback.count

ViewCountCheckpointWorker.checkpoint
  -> Redis 전체 카운터 SCAN
  -> MySQL GREATEST(view_count, snapshot)

InactiveCounterEvictor.evict
  -> 비활성 카운터 최종 체크포인트
  -> (count, last_counted_at) CAS 삭제
```

## 적용한 리팩토링

- 요청 집계, 최초 적재, 체크포인트, 비활성 제거, 장애 폴백을 서로 다른 역할 컴포넌트로 유지했습니다.
- Lua는 한 요청 안의 Redis 상태 변경만 책임지고 MySQL I/O를 포함하지 않습니다.
- `dirty + generation`, Micrometer 계측과 스케줄 설정은 핵심 정확성 경계가 아니므로 제외했습니다.

## 핵심 코드

1. [TotalViewCountCounter.java](./src/main/java/cluverse/meta/service/implement/TotalViewCountCounter.java)
2. [ViewCountInitializer.java](./src/main/java/cluverse/meta/service/implement/ViewCountInitializer.java)
3. [TotalViewCountRepository.java](./src/main/java/cluverse/meta/repository/TotalViewCountRepository.java)
4. [count_total.lua](./src/main/resources/redis/count_total.lua)
5. [ViewCountCheckpointWorker.java](./src/main/java/cluverse/meta/service/implement/ViewCountCheckpointWorker.java)
6. [InactiveCounterEvictor.java](./src/main/java/cluverse/meta/service/implement/InactiveCounterEvictor.java)
7. [LocalViewCountFallback.java](./src/main/java/cluverse/meta/service/implement/LocalViewCountFallback.java)
8. [PostViewCountRepository.java](./src/main/java/cluverse/meta/repository/PostViewCountRepository.java)
9. [LocalViewCountRecovery.java](./src/main/java/cluverse/meta/service/implement/LocalViewCountRecovery.java)
10. [TotalViewCountCounterTest.java](./src/test/java/cluverse/meta/service/implement/TotalViewCountCounterTest.java)
11. [ViewCountInitializerTest.java](./src/test/java/cluverse/meta/service/implement/ViewCountInitializerTest.java)
12. [InactiveCounterEvictorTest.java](./src/test/java/cluverse/meta/service/implement/InactiveCounterEvictorTest.java)

## 검증과 남은 한계

- 정상 체크포인트 중 화면의 표시 원천이 Redis 하나로 유지되어 조회수가 감소하지 않습니다.
- 단일 핫 레코드에서 1,000 TPS 이상을 처리했습니다.
- Redis 장애와 인스턴스 종료가 겹치면 로컬 delta 일부가 유실될 수 있습니다.
- 전체 카운터의 메모리, 초기화 경쟁, 전체 순회와 비활성 제거 운영 비용이 남습니다.
- 핵심 계약 테스트는 포함하지만 Redis·MySQL 통합 환경이 없어 이 저장소에서는 실행하지 않습니다.
