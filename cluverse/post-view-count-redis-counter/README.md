# 게시글 조회수 Redis 집계와 전체 카운터 설계

MySQL 단일 카운터의 처리량을 넘는 조회 트래픽과 브라우저별 중복 판정을 Redis로 옮기면서, 시간 기반 증분·임계치 증분·전체 조회수 카운터를 비교한 사례입니다. Redis와 MySQL 사이의 증분 handoff가 화면 조회수를 역행시킨다는 문제를 확인하고, Redis 전체값을 표시 원천으로 고정하는 구조를 선택했습니다.

전체 Cluverse 서버에서 조회수 집계 전략을 구분하는 Service, Implement, Repository, Lua 스크립트와 핵심 계약 테스트만 선별하고 기존 사례의 패키지·의존 방향으로 정리했습니다.

## 관련 글

- [Cluverse #5: Redis 조회수 집계 방식 세 가지와 전체 카운터 설계](https://velog.io/@bebeis/Cluverse-5-cache-counter)

## 문제

- MySQL 원자적 UPDATE는 하나의 `postId`에 쓰기가 집중되면 약 280 TPS에서 레코드 락 대기가 증가합니다.
- 스케일아웃 환경에서 `(postId, cookieId)` 중복 판정을 공유하려면 중앙 저장소가 필요합니다.
- Redis delta를 MySQL에 옮기는 순간 두 저장소를 하나의 트랜잭션으로 묶을 수 없어 표시값이 감소하거나 중복 합산될 수 있습니다.
- 임계치에 도달하지 못한 delta는 영구 잔류하거나 TTL 삭제 시 유실됩니다.
- Redis 전체 카운터는 표시값을 단조 증가시키지만 초기화 경쟁, 내구성 체크포인트와 메모리 회수 책임이 추가됩니다.

## 해결

1. MySQL 원자적 UPDATE를 비교 기준선으로 유지합니다.
2. 시간 기반 delta를 `SCAN → GETDEL → DB 배치 반영`하지만 표시값 역행 때문에 폐기합니다.
3. 임계치 기반 delta도 저조회 글과 동일한 handoff 문제 때문에 단독 선택지에서 제외합니다.
4. MySQL 누적값을 필요할 때 Redis에 적재하고 유효 조회를 전체 카운터에 직접 반영합니다.
5. 조회 인정 Lua가 중복 방지 락과 전체 카운터 증가를 원자적으로 실행합니다.
6. 전체 순회 체크포인트는 `GREATEST`로 오래된 스냅샷이 MySQL 값을 내리지 못하게 합니다.
7. 비활성 카운터는 최종 체크포인트 뒤 `(count, last_counted_at)` CAS가 성공할 때만 제거합니다.
8. Redis 장애 시 MySQL로 트래픽을 넘기지 않고 인스턴스 로컬 delta로 격리한 뒤 복구합니다.

## 버전별 코드

- [`v1-mysql-atomic-update/`](./v1-mysql-atomic-update/): 요청마다 MySQL 원자적 UPDATE
- [`v2-time-based-delta/`](./v2-time-based-delta/): Redis delta와 주기 flush
- [`v3-threshold-delta/`](./v3-threshold-delta/): 임계치 도달 시 delta flush
- [`v4-total-counter/`](./v4-total-counter/): Redis 전체 카운터와 MySQL 체크포인트

각 버전은 같은 `PostViewCountCommandService` 진입점을 유지하고, 조회수 저장 흐름은 `cluverse.meta.service.implement`가 소유합니다. Service는 Repository를 직접 참조하지 않으며 Redis Lua와 DB 배치 경계는 Repository 밖으로 노출하지 않습니다.

공통 빌드 설정, Controller, 쿠키 발급기, 게시글 존재 검증, API 응답 래퍼와 모니터링 설정은 생략해 독립 빌드를 보장하지 않습니다.

## 결과

- MySQL 단일 핫 레코드 기준선: 약 280 TPS
- 시간 기반 delta 200 RPS 회귀 측정: 역행 이벤트 10회, 역행 관측 샘플 411개
- 같은 측정의 최대 하락폭: 12,103, 최대 회복 시간: 2,250ms
- Redis 전체 카운터 단일 핫 레코드: 1,000 TPS 이상 처리
- 전체 카운터는 정상 체크포인트 중 표시값을 감소시키지 않음

세부 인프라, 부하 조건, Redis QPS와 체크포인트 관측 그래프는 관련 글에 기록되어 있습니다.
