# Cluverse

Cluverse 프로젝트를 개발하며 마주친 문제와 해결 과정을 사례별로 정리합니다.

## 문제 해결 사례

| 사례 | 다룬 내용 | 관련 글 |
| --- | --- | --- |
| [게시글 목록 조회 설계 및 개선](./post-list-query-optimization/) | Offset·COUNT 병목, Redis 캐시, 커서 페이징 | [Velog](https://velog.io/@bebeis/cluverse-3-post-list) |
| [게시글 조회수 DB 동시성 및 성능 개선](./post-view-count-db-optimization/) | 낙관적 락, 비관적 락, 원자적 UPDATE, fsync 병목 | [Velog](https://velog.io/@bebeis/cluverse-4-post-db) |
| [게시글 조회수 Redis 집계와 전체 카운터 설계](./post-view-count-redis-counter/) | 증분 flush 역행, 전체 카운터, 체크포인트, 비활성 제거 | [Velog](https://velog.io/@bebeis/Cluverse-5-cache-counter) |
| [로컬맵 외부 장소 검증과 트랜잭션 분리](./local-map-async-place-verification/) | 외부 API 대기 격리, AFTER_COMMIT 이벤트, 비동기 완료 | [Velog](https://velog.io/@bebeis/Cluverse-8-local-map) |
| [이미지 처리 병렬화와 DB–S3 정합성](./image-processing-db-s3-consistency/) | Lambda 분리, 제한 병렬 처리, 예약·보상·멱등성 | [Velog](https://velog.io/@bebeis/cluverse-11-image-optimize) |
