# 게시글 조회수 DB 동시성 및 성능 개선

조회할 때마다 쓰기가 발생하는 게시글 조회수를 RDB에서 안전하게 증가시키면서, 단일 핫 레코드의 처리량을 제한하는 원인을 측정한 사례입니다. 애플리케이션 모니터 락에서 DB 비관적 락을 거쳐 원자적 UPDATE로 락 구간을 줄였고, 남은 상한이 락 연산보다 커밋 시점의 fsync에서 온다는 점을 확인했습니다.

전체 Cluverse 서버에서 조회수 증가 전략을 비교하는 데 필요한 Entity, Repository, Implement, Service와 핵심 계약 테스트만 선별하고, 기존 Cluverse 사례의 컨벤션에 맞춰 리팩토링했습니다.

## 관련 글

- [Cluverse #4: 게시글 조회수 설계 및 성능 개선 - DB만으로 해결해보기](https://velog.io/@bebeis/cluverse-4-post-db)

## 문제

- 조회수는 정합성 요구가 낮지만 게시글 조회마다 쓰기가 발생합니다.
- 애플리케이션 모니터 락은 서버 인스턴스가 늘어나면 인스턴스 사이의 갱신을 보호하지 못합니다.
- 비관적 락은 `SELECT FOR UPDATE → 애플리케이션 → UPDATE → COMMIT` 동안 같은 레코드의 다음 요청을 기다리게 합니다.
- 같은 레코드에 쓰기가 몰리면 커넥션을 늘려도 커밋이 직렬화되어 처리량이 증가하지 않습니다.

조회수는 게시글과 정합성 요구, 변경 빈도, 접근 패턴이 다르므로 `post_view_count` 테이블로 분리합니다. 조회수 갱신의 레코드 락이 게시글 본문 수정을 막지 않도록 경계도 분리됩니다.

## 해결

1. 스케일아웃으로 JVM 모니터 락을 사용할 수 없게 된 뒤, `@Version`과 재시도를 사용하는 낙관적 락을 측정합니다.
2. 모든 인스턴스가 공유하는 DB의 `SELECT FOR UPDATE`로 제어 지점을 옮깁니다.
3. 최종적으로 `view_count = view_count + 1` 원자적 UPDATE 한 문장으로 읽기와 애플리케이션 왕복을 제거합니다.
4. DB 직접 실험으로 남은 단일 레코드 상한이 커밋마다 발생하는 fsync 비용임을 확인합니다.

Redis write-back과 HyperLogLog는 글에서 후속 설계로만 다루므로 이 사례의 버전 코드에는 포함하지 않습니다.

## 버전별 코드

- [`v1-optimistic-lock/`](./v1-optimistic-lock/): `@Version` 충돌 감지와 새 트랜잭션 재시도
- [`v2-pessimistic-lock/`](./v2-pessimistic-lock/): DB `SELECT FOR UPDATE`와 JPA 변경 감지
- [`v3-atomic-update/`](./v3-atomic-update/): 원자적 UPDATE로 락 보유 구간 최소화

각 버전은 같은 `PostViewCountCommandService` 계약을 유지하고 `cluverse.meta.service.implement.PostMetaWriter`의 증가 전략과 Repository 구현만 바꿉니다. 조회수 저장 모델은 기존 목록 조회 사례와 동일하게 `cluverse.meta.domain`이 소유하며, Service가 Repository를 직접 참조하지 않습니다.

비교를 위해 필요한 타입은 버전마다 중복해서 포함합니다. 공통 빌드 설정, Controller, 게시글 상세 조회 조립과 주변 도메인은 생략해 독립 빌드를 보장하지 않습니다.

## 결과

- 비관적 락 100 RPS: 평균 응답 시간 약 127ms
- 비관적 락 300 RPS: 처리량 약 200 TPS에서 정체, 응답 시간 중앙값 약 470ms
- 원자적 UPDATE 100 RPS: 평균 응답 시간 51ms
- DB 직접 단일 레코드 UPDATE: 1커넥션 285 TPS, 20커넥션 288 TPS
- DB 직접 서로 다른 레코드 UPDATE: 20커넥션 3,128 TPS로 단일 레코드 대비 약 11배

측정은 애플리케이션 서버와 MySQL을 각각 AWS t3.small(2 vCPU, 2GB)에 두고, 게시글 100만 건을 시드한 환경에서 수행했습니다. 세부 부하 조건과 그래프는 관련 글에 기록되어 있습니다.
