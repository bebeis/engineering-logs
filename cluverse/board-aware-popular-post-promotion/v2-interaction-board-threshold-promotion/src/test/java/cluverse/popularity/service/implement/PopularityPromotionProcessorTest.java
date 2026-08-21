package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularPostPromotion;
import cluverse.popularity.domain.PopularityFinalizationTarget;
import cluverse.popularity.domain.PopularityPolicy;
import cluverse.popularity.domain.PopularitySnapshot;
import cluverse.popularity.domain.PopularityTrigger;
import cluverse.popularity.domain.StoredPopularityPolicy;
import cluverse.popularity.repository.BoardPopularityPolicyRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PopularityPromotionProcessorTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant NOW = Instant.parse("2026-05-25T12:00:00Z");

    @Test
    void 변경된_게시글_하나를_게시판별_기준으로_승격한다() {
        List<PopularPostPromotion> promotions = new ArrayList<>();
        PopularitySnapshot snapshot = new PopularitySnapshot(
                1, 10, LocalDateTime.ofInstant(NOW, ZONE).minusHours(1), 7, 0, false);
        PopularityPromotionProcessor processor = processor(snapshot, 20, promotions);

        boolean promoted = processor.evaluate(1, PopularityTrigger.LIKE);

        assertTrue(promoted);
        assertEquals(21, promotions.getFirst().scoreAtPromotion());
        assertEquals(20, promotions.getFirst().promotionScoreThreshold());
    }

    @Test
    void 게시판_기준에_못_미치면_상태를_남기지_않고_다음_상호작용을_기다린다() {
        List<PopularPostPromotion> promotions = new ArrayList<>();
        PopularitySnapshot snapshot = new PopularitySnapshot(
                1, 10, LocalDateTime.ofInstant(NOW, ZONE).minusHours(1), 6, 0, false);

        boolean promoted = processor(snapshot, 20, promotions).evaluate(1, PopularityTrigger.COMMENT);

        assertFalse(promoted);
        assertTrue(promotions.isEmpty());
    }

    private PopularityPromotionProcessor processor(
            PopularitySnapshot snapshot,
            long threshold,
            List<PopularPostPromotion> promotions
    ) {
        PopularityProperties properties = properties();
        BoardPopularityPolicyRepository repository = new BoardPopularityPolicyRepository() {
            public Optional<StoredPopularityPolicy> findByBoardId(long boardId) {
                return Optional.of(new StoredPopularityPolicy(
                        boardId, new PopularityPolicy(threshold), 100, "DISTRIBUTION", snapshot.createdAt()));
            }
            public void save(StoredPopularityPolicy policy) { }
        };
        PopularPostWriter writer = new PopularPostWriter() {
            public void promote(PopularPostPromotion promotion) { promotions.add(promotion); }
            public List<PopularityFinalizationTarget> findDue(LocalDateTime now, int limit) { return List.of(); }
            public boolean finalizeIfPending(long id, long score, long likes, long comments, LocalDateTime at) {
                return false;
            }
        };
        return new PopularityPromotionProcessor(
                postId -> Optional.of(snapshot),
                new PopularityPolicyStore(repository, properties),
                writer,
                properties,
                Clock.fixed(NOW, ZONE)
        );
    }

    private PopularityProperties properties() {
        return new PopularityProperties(
                3, 2, 100, Duration.ofHours(48), Duration.ofDays(7),
                0.98, 3, 0.3, 100
        );
    }
}
