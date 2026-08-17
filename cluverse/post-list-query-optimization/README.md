# 게시글 목록 조회 설계 및 개선

인덱스를 사용한 뒤에도 느렸던 게시글 목록 조회를 분석하고, Offset과 COUNT에서 발생한 병목을 줄인 사례입니다. Redis 캐시와 커서 페이징을 적용하는 과정도 함께 다룹니다.

## 관련 글

- [Cluverse #3: 게시글 목록 조회 설계 및 개선 과정](https://velog.io/@bebeis/cluverse-3-post-list)

## 문제

- 인덱스를 사용해도 게시글 목록 조회 성능이 충분히 개선되지 않았습니다.
- Offset 기반 페이지네이션과 COUNT 쿼리가 데이터 증가에 따라 병목이 되었습니다.

## 해결

- 조회 흐름에서 발생하는 병목을 단계별로 확인합니다.
- 반복 조회 비용을 줄이기 위해 Redis 캐시를 적용합니다.
- 깊은 페이지 조회 비용을 줄이기 위해 커서 기반 페이지네이션을 적용합니다.

## 버전별 코드

- [`v1-offset-pagination/`](./v1-offset-pagination/): Offset 기반 페이지네이션
- [`v2-redis-cache/`](./v2-redis-cache/): 반복 조회 비용을 줄이기 위한 Redis 캐시 적용
- [`v3-cursor-pagination/`](./v3-cursor-pagination/): 깊은 페이지 조회 비용을 줄이는 커서 기반 페이지네이션

구체적인 측정 조건과 결과는 관련 글에서 확인할 수 있습니다.
