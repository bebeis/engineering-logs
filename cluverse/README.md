# Cluverse

Cluverse 프로젝트를 개발하며 마주친 문제와 해결 과정을 사례별로 정리합니다.

## 문제 해결 사례

| 사례 | 다룬 내용 | 관련 글 |
| --- | --- | --- |
| [게시글 목록 조회 설계 및 개선](./post-list-query-optimization/) | Offset·COUNT 병목, Redis 캐시, 커서 페이징 | [Velog](https://velog.io/@bebeis/cluverse-3-post-list) |
| [게시글 조회수 DB 동시성 및 성능 개선](./post-view-count-db-optimization/) | 낙관적 락, 비관적 락, 원자적 UPDATE, fsync 병목 | [Velog](https://velog.io/@bebeis/cluverse-4-post-db) |
| [게시글 조회수 Redis 집계와 전체 카운터 설계](./post-view-count-redis-counter/) | 증분 flush 역행, 전체 카운터, 체크포인트, 비활성 제거 | [Velog](https://velog.io/@bebeis/Cluverse-5-cache-counter) |
| [게시판별 기준의 준실시간 인기글 승격](./board-aware-popular-post-promotion/) | 일일 전역 스캔, 상호작용 기반 판정, 게시판별 동적 기준, 멱등 승격 | [Velog](https://velog.io/@bebeis/cluverse-6-popular-post) |
| [n-depth 댓글의 요청 비용 제한과 경로 조회](./bounded-comment-tree-pagination/) | 전체 서브트리 응답, 직계 자식 커서, 재귀 CTE, 영구 path 비교, 삭제 직렬화 | [Velog](https://velog.io/@bebeis/Cluverse-7-comment-path-enumeration) |
| [로컬맵 외부 장소 검증과 트랜잭션 분리](./local-map-async-place-verification/) | 외부 API 대기 격리, AFTER_COMMIT 이벤트, 비동기 완료 | [Velog](https://velog.io/@bebeis/Cluverse-8-local-map) |
| [홈 최근 댓글 글의 Top-K 조회 최적화](./home-recent-commented-post-top-k/) | GROUP BY·MAX, Loose Index Scan, 후보 캐시, 쓰기 시 활동 투영 | [Velog](https://velog.io/@bebeis/cluverse-9-home-feed-group-by) |
| [이미지 처리 병렬화와 DB–S3 정합성](./image-processing-db-s3-consistency/) | Lambda 분리, 제한 병렬 처리, 예약·보상·멱등성 | [Velog](https://velog.io/@bebeis/cluverse-11-image-optimize) |
