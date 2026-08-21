# v2 - Interaction-Based Board Threshold Promotion

좋아요·댓글 저장 직후 변경된 게시글 하나만 동기로 판정하고, 게시판별 최근 반응 분포에서 계산한 기준으로 인기글을 승격합니다.

## 이전 버전과의 차이

- 인기글 직접 선발 일일 배치를 제거하고 점수가 변하는 상호작용 시점에 단건 판정합니다.
- 전역 고정 기준 대신 게시판별 분위수·최소 표본·스무딩 정책을 사용합니다.
- 일일 배치는 게시판 정책 갱신만 담당합니다.
- 승격 실패를 원래 좋아요·댓글 저장 결과와 격리합니다.
- 48시간 뒤 최종 점수를 확정하고, 정책 표본에는 전체 노출 전 점수를 사용합니다.

## 요청과 생명주기

```text
PostReactionService.likePost / CommentService.create
  -> 좋아요·댓글 저장 성공
  -> PopularityPromotionService.tryPromote
    -> postId snapshot 단건 조회
    -> 게시판 정책 메모리 캐시 조회, miss면 MySQL 조회
    -> 점수 계산과 48시간 검사
    -> 기준 통과 시 멱등 UPSERT
    -> 승격 실패는 기록하고 원 요청에는 전파하지 않음

정책 배치
  -> 게시판별 최근 7일 표본 조회
  -> 분위수 또는 기본 기준 계산
  -> 이전 기준과 스무딩
  -> MySQL 저장 후 메모리 캐시 교체

48시간 경과
  -> 미확정 인기글 조회
  -> 현재 좋아요·댓글 snapshot으로 최종 점수 조건부 확정
```

## 핵심 코드

1. [PostReactionService.java](./src/main/java/cluverse/reaction/service/PostReactionService.java)
2. [CommentService.java](./src/main/java/cluverse/comment/service/CommentService.java)
3. [PopularityPromotionService.java](./src/main/java/cluverse/popularity/service/PopularityPromotionService.java)
4. [PopularityPromotionProcessor.java](./src/main/java/cluverse/popularity/service/implement/PopularityPromotionProcessor.java)
5. [BoardPopularityPolicyCalculator.java](./src/main/java/cluverse/popularity/service/implement/BoardPopularityPolicyCalculator.java)
6. [PopularityPolicyRefreshProcessor.java](./src/main/java/cluverse/popularity/service/implement/PopularityPolicyRefreshProcessor.java)
7. [PopularityPolicyStore.java](./src/main/java/cluverse/popularity/service/implement/PopularityPolicyStore.java)
8. [PopularityFinalizationProcessor.java](./src/main/java/cluverse/popularity/service/implement/PopularityFinalizationProcessor.java)
9. [V2__create_board_aware_popularity.sql](./src/main/resources/db/migration/V2__create_board_aware_popularity.sql)
10. [PopularityPromotionProcessorTest.java](./src/test/java/cluverse/popularity/service/implement/PopularityPromotionProcessorTest.java)
11. [BoardPopularityPolicyCalculatorTest.java](./src/test/java/cluverse/popularity/service/implement/BoardPopularityPolicyCalculatorTest.java)
12. [PopularityPromotionServiceTest.java](./src/test/java/cluverse/popularity/service/PopularityPromotionServiceTest.java)

## 적용한 리팩토링

- 좋아요·댓글 Service는 각 저장 Processor가 성공한 뒤에만 인기글 Service를 호출합니다.
- 상호작용과 인기글은 실패 정책이 다르므로 승격 예외를 경계에서 종료합니다.
- 게시판 기준 계산을 순수 객체로 분리해 분위수, 최소 표본, 스무딩과 노출 효과 차단을 독립 검증합니다.
- 정책 저장소는 MySQL을 원본으로 두고 메모리 캐시는 빠른 읽기만 담당합니다.
- 승격과 최종 확정은 Writer가 소유하는 UPSERT·조건부 갱신 계약으로 표현했습니다.

## 검증과 남은 한계

- 게시판별 기준 통과·미달, 승격 실패 격리, `score_at_promotion` 표본과 스무딩을 테스트합니다.
- 좋아요 p95 +6.07ms, 댓글 p95 +3.78ms의 부가 비용에서 Hikari pending은 발생하지 않았습니다.
- 정책 갱신만으로 기존 글을 소급 승격하지 않으며 다음 상호작용부터 새 기준을 적용합니다.
- 게시판별 가중치·분위수·최소 표본과 스무딩 비율은 운영 데이터로 계속 조정해야 합니다.
- 어뷰징 탐지는 계정 신뢰도와 행동 패턴을 다루는 별도 영역으로 남깁니다.
- main 소스는 `javac` 컴파일을 확인했으며 JUnit 실행 환경은 포함하지 않습니다.
