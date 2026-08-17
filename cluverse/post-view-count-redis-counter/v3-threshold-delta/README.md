# v3 - Threshold Delta

게시글별 Redis delta가 임계치에 도달하면 즉시 MySQL에 반영해 핫 레코드의 UPDATE 횟수를 줄입니다.

## 이전 버전과의 차이

- 주기적인 전체 탐색 대신 `INCR` 반환값으로 임계치 도달을 판단합니다.
- 임계치에 도달한 게시글만 요청 경로에서 delta를 MySQL로 옮깁니다.
- 임계치 미만의 저조회 글과 표시값 handoff 문제는 그대로 남습니다.

## 요청 흐름

```text
PostViewCountCommandService.increaseViewCount
  -> DeltaViewCountCounter.count
    -> count_delta.lua
    -> delta >= threshold 이면 GETDEL
    -> PostMetaWriter.applyViewCountDeltas
    -> MySQL 누적값 + 남은 Redis delta
```

## 적용한 리팩토링

- V3에 필요한 임계치 경로만 남기고 시간 기반 `SCAN`과 스케줄러를 제거했습니다.
- 임계치 판정과 실패 시 복원은 Counter, Redis 원자 연산은 Repository와 Lua가 소유합니다.

## 핵심 코드

1. [DeltaViewCountCounter.java](./src/main/java/cluverse/meta/service/implement/DeltaViewCountCounter.java)
2. [DeltaViewCountRepository.java](./src/main/java/cluverse/meta/repository/DeltaViewCountRepository.java)
3. [count_delta.lua](./src/main/resources/redis/count_delta.lua)
4. [DeltaViewCountCounterTest.java](./src/test/java/cluverse/meta/service/implement/DeltaViewCountCounterTest.java)

## 검증과 폐기 이유

- 임계치에 못 미친 delta는 별도 시간 조건 없이는 MySQL에 반영되지 않습니다.
- TTL로 제거하면 조회수가 유실되고 시간 스위퍼를 추가하면 V2와 같은 하이브리드로 수렴합니다.
- 증분 handoff의 표시값 역행·중복 문제도 유지되므로 단독 선택지에서 제외했습니다.
- 핵심 계약 테스트는 포함하지만 공통 빌드 설정이 없어 실행하지 않습니다.
