# v1 - Daily Global Threshold Scan

매일 오전 1시에 최근 48시간 게시글 전체를 키셋으로 순회하고, 모든 게시판에 같은 전역 점수 기준을 적용하는 비교 기준선입니다.

## 처리 흐름

```text
PopularityPromotionService.promoteDaily
  -> PopularityBatchProcessor.runDaily
    -> 최근 48시간 snapshot을 (createdAt, postId) 키셋으로 조회
    -> 각 snapshot의 좋아요 × 3 + 댓글 × 2 계산
    -> 전역 기준 통과 시 PopularPostWriter.promote
       # (algorithm_version, post_id) UPSERT
```

## 핵심 코드

1. [PopularityPromotionService.java](./src/main/java/cluverse/popularity/service/PopularityPromotionService.java)
2. [PopularityBatchProcessor.java](./src/main/java/cluverse/popularity/service/implement/PopularityBatchProcessor.java)
3. [PopularityPromotionProcessor.java](./src/main/java/cluverse/popularity/service/implement/PopularityPromotionProcessor.java)
4. [PopularityScore.java](./src/main/java/cluverse/popularity/service/implement/PopularityScore.java)
5. [V1__create_popular_post.sql](./src/main/resources/db/migration/V1__create_popular_post.sql)
6. [PopularityBatchProcessorTest.java](./src/test/java/cluverse/popularity/service/implement/PopularityBatchProcessorTest.java)
7. [PopularityPromotionProcessorTest.java](./src/test/java/cluverse/popularity/service/implement/PopularityPromotionProcessorTest.java)

## 적용한 리팩토링

- Service는 배치 유스케이스만 노출하고 snapshot 조회와 승격 쓰기는 Reader·Writer 경계로 내렸습니다.
- 점수 계산을 프레임워크 없는 `PopularityScore`로 분리했습니다.
- Offset 대신 `(createdAt, postId)` 키셋으로 같은 작성 시각의 게시글도 빠뜨리지 않고 순회합니다.
- 삭제 여부와 48시간 만료 규칙을 승격 Processor 한곳에서 검사합니다.

## 검증과 한계

- 테스트는 여러 chunk를 키셋으로 끝까지 순회하는 계약과 48시간 만료를 검증합니다.
- `popular_post`의 unique 제약으로 배치 재실행을 멱등하게 만듭니다.
- 작성 시각에 따라 승격 지연이 달라지고, 반응이 바뀌지 않은 게시글도 매일 다시 읽습니다.
- 전역 기준은 게시판별 평소 반응 차이를 표현하지 못합니다.
- main 소스는 `javac` 컴파일을 확인했으며 JUnit 실행 환경은 포함하지 않습니다.
