# v2 - Time-based Delta

중복 판정을 통과한 조회를 Redis delta로 누적하고, 스케줄러가 주기적으로 MySQL에 증가량을 반영합니다.

## 요청과 flush 흐름

```text
PostViewCountCommandService.increaseViewCount
  -> DeltaViewCountCounter.count
    -> count_delta.lua              # SET NX EX + INCR
    -> MySQL 누적값 + Redis delta   # 화면 표시값

ViewCountScheduler.flushTimeBasedDelta
  -> DeltaViewCountCounter.flushTimeBased
    -> SCAN → get_and_delete.lua
    -> PostMetaWriter.applyViewCountDeltas
    -> 실패하면 Redis delta 복원
```

## 적용한 리팩토링

- V2에 필요한 시간 기반 경로만 남겨 버전 분기 없이 읽히도록 정리했습니다.
- 중복 판정과 delta 증가는 Lua, Redis 탐색과 복원은 Repository, handoff 정책은 Counter가 소유합니다.
- DB 배치 반영은 `PostMetaWriter` 뒤로 격리했습니다.

## 핵심 코드

1. [DeltaViewCountCounter.java](./src/main/java/cluverse/meta/service/implement/DeltaViewCountCounter.java)
2. [DeltaViewCountRepository.java](./src/main/java/cluverse/meta/repository/DeltaViewCountRepository.java)
3. [count_delta.lua](./src/main/resources/redis/count_delta.lua)
4. [get_and_delete.lua](./src/main/resources/redis/get_and_delete.lua)
5. [DeltaViewCountCounterTest.java](./src/test/java/cluverse/meta/service/implement/DeltaViewCountCounterTest.java)

## 검증과 폐기 이유

- `GETDEL` 직후부터 MySQL 커밋 전까지 화면 값이 감소합니다.
- 200 RPS 측정에서 역행 이벤트 10회, 최대 하락폭 12,103, 최대 회복 시간 2,250ms가 관측됐습니다.
- DB 실패가 명확하면 delta를 복원하지만 커밋 성공 뒤 응답만 잃는 모호한 실패는 중복 반영 가능성이 남습니다.
- 핵심 계약 테스트는 포함하지만 전체 빌드와 스케줄러 환경이 없어 실행하지 않습니다.
