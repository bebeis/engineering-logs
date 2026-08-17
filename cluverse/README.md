# Cluverse

Cluverse 프로젝트를 개발하며 마주친 문제와 해결 과정을 사례별로 정리합니다.

## 문제 해결 사례

| 사례 | 다룬 내용 | 관련 글 |
| --- | --- | --- |
| [게시글 목록 조회 설계 및 개선](./post-list-query-optimization/) | Offset·COUNT 병목, Redis 캐시, 커서 페이징 | [Velog](https://velog.io/@bebeis/cluverse-3-post-list) |
| [게시글 조회수 DB 동시성 및 성능 개선](./post-view-count-db-optimization/) | 모니터 락, 비관적 락, 원자적 UPDATE, fsync 병목 | [Velog](https://velog.io/@bebeis/cluverse-4-post-db) |
