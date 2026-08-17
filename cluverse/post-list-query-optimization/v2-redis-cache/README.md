# v2 - Redis Cache

앞쪽 최신순 요청이 반복하는 ID 선정과 상한 COUNT를 Redis로 흡수한 버전입니다. 캐시의 기반이 되는
DB 경로도 ID 선조회와 상한 COUNT 방식으로 정리했습니다.

## 이전 버전과 달라진 점

- 커버링 인덱스에서 `size + 1`개의 ID를 먼저 고른 뒤 선택된 게시글만 projection합니다.
- 전체 개수 대신 현재 페이지 블록에 필요한 `searchLimit`까지만 셉니다.
- 최신 201개 ID를 Redis Sorted Set에 저장해 앞쪽 요청의 두 DB 쿼리를 생략합니다.

## 요청 흐름

```text
cache hit   -> Redis ID slice + cached count -> DB projection
cache miss  -> warmup lock -> DB latest 201 IDs -> version check -> Redis replace -> DB projection
Redis error -> DB ID slice + capped count -> DB projection
```

Redis는 데이터 원천이 아닙니다. 연결·Lua 실행이 실패하거나 다른 요청이 워밍 중이면 기다리지 않고 DB로
폴백합니다.

## 핵심 코드 읽는 순서

1. [PostListQueryService.java](./src/main/java/cluverse/post/service/PostListQueryService.java)
   - 페이지 블록용 상한을 계산하는 비즈니스 흐름
2. [PostListReader.java](./src/main/java/cluverse/post/service/implement/PostListReader.java)
   - 적중, 미스, 워밍 경합, 버전 충돌, 장애 폴백을 조립하는 Implement
3. [PostPageQueryRepository.java](./src/main/java/cluverse/post/repository/PostPageQueryRepository.java)
   - 커버링 인덱스 ID 선조회와 derived table 상한 COUNT
4. [PostSummaryQueryRepository.java](./src/main/java/cluverse/post/repository/PostSummaryQueryRepository.java)
   - 선정된 ID만 projection하고 ID 순서로 재조립
5. [PostListCacheRepository.java](./src/main/java/cluverse/post/repository/PostListCacheRepository.java)
   - Sorted Set, 워밍 락, 버전, 원자적 Lua 호출
6. [PostListCacheInvalidator.java](./src/main/java/cluverse/post/service/implement/PostListCacheInvalidator.java)
   - 게시글 쓰기 커밋 이후 무효화, 실패 시 TTL 복구
7. [PostListReaderTest.java](./src/test/java/cluverse/post/service/implement/PostListReaderTest.java)
   - DB 쿼리 생략, 장애 폴백, 버전 기반 워밍 계약

## Redis 계약

- 캐시 범위: 최신순 상위 201개 ID
- 정렬: `createdAt`을 score, 19자리 패딩 `postId`를 member로 사용
- TTL: 3분
- 워밍 락 lease: 2초
- 쓰기 경합: 워밍 시작 전에 읽은 버전과 저장 시점 버전이 같을 때만 교체
- 무효화: 쓰기 트랜잭션 `AFTER_COMMIT`

Lua 스크립트는 [redis/](./src/main/resources/redis/)에서 확인할 수 있습니다. 실제 쓰기 코드의 이벤트 발행
지점과 RedisScript Bean 설정은 목록 조회 사례 밖이므로 포함하지 않았습니다.

## 측정 결과

| 목표 | 평균 | p99 | 오류율 | DB CPU 평균 |
| ---: | ---: | ---: | ---: | ---: |
| 300 TPS | 30.84ms | 62.92ms | 0% | 47.64% |
| 400 TPS | 36.98ms | 195.58ms | 0% | 64.09% |
| 500 TPS | 39.13ms | 173ms | 0% | 80.53% |

원본의 Micrometer 계측과 카테고리별 캐시 변형은 핵심 제어 흐름을 가려 이 발췌에서 제외했습니다.
