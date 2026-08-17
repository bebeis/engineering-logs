# 게시글 목록 조회 설계 및 개선

인덱스를 사용한 뒤에도 느렸던 게시글 목록 조회를 분석하고, 행 조립 시점·COUNT 범위·반복 조회·탐색 방식을 차례로 바꾼 사례입니다.

코드는 전체 Cluverse 서버가 아니라 각 단계의 목록 조회에 참여하는 Controller, Service, Implement,
Repository와 핵심 테스트만 남겼습니다. 인증, 게시글 쓰기, 회원 권한, 상세 조회는 포함하지 않습니다.

## 관련 글

- [Cluverse #3: 게시글 목록 조회 설계 및 개선 과정](https://velog.io/@bebeis/cluverse-3-post-list)

## 문제

- 깊은 페이지에서 버릴 행까지 projection과 여러 조인을 수행했습니다.
- 커버링 인덱스를 사용해도 Offset 이동량은 페이지 깊이에 비례했습니다.
- 정확한 전체 `COUNT(*)`가 첫 페이지에서도 전체 조건 범위를 스캔했습니다.
- 앞쪽 페이지에 트래픽이 몰려 같은 ID 구간과 상한 COUNT를 반복 조회했습니다.

## 해결

- 페이지 ID를 커버링 인덱스로 먼저 고르고 선택된 행만 조립합니다.
- 페이지 블록을 그리는 데 필요한 상한까지만 셉니다.
- 최신 201개 ID를 Redis Sorted Set에 저장해 앞쪽 요청의 ID 조회와 COUNT를 생략합니다.
- 날짜를 진입 앵커로 사용하고 `(createdAt, postId)` 커서로 이동해 Offset을 제거합니다.

## 버전별 코드

- [`v1-offset-pagination/`](./v1-offset-pagination/): projection JOIN과 전체 COUNT를 사용하는 기준선
- [`v2-redis-cache/`](./v2-redis-cache/): ID 선조회·상한 COUNT와 최신 ID Redis 캐시
- [`v3-cursor-pagination/`](./v3-cursor-pagination/): 날짜 앵커와 양방향 튜플 커서

각 디렉터리는 비교를 쉽게 하기 위해 필요한 타입을 중복해서 포함합니다. 독립 빌드는 보장하지 않으며,
QueryDSL 생성 타입과 목록 조회 설명에 필요하지 않은 연관 Entity는 생략했습니다.

## 결과

- 캐시 없는 혼합 조회 300 TPS: 평균 172ms, p99 1초, DB CPU 92%
- Redis 캐시 적용 400 TPS: 평균 36.98ms, p99 195.58ms, 오류율 0%, DB CPU 평균 64.09%
- Redis 캐시 적용 500 TPS: 평균 39.13ms, p99 173ms, 오류율 0%, DB CPU 평균 80.53%
- 커서 세션 300 TPS 이상: 평균 81ms, p99 444~469ms

인프라, 데이터 분포, 워밍업과 세부 측정 조건은 관련 글에 기록했습니다.
