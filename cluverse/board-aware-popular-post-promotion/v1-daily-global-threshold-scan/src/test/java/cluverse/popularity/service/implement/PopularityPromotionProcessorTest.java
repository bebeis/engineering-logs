package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularPostPromotion;
import cluverse.popularity.domain.PopularitySnapshot;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PopularityPromotionProcessorTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant NOW = Instant.parse("2026-05-25T01:00:00Z");

    @Test
    void 전역_기준을_넘은_48시간_이내_게시글을_승격한다() {
        List<PopularPostPromotion> promotions = new ArrayList<>();
        PopularityPromotionProcessor processor = processor(promotions);
        PopularitySnapshot snapshot = new PopularitySnapshot(
                1, 10, LocalDateTime.ofInstant(NOW, ZONE).minusHours(1), 34, 0, false);

        boolean promoted = processor.evaluate(snapshot);

        assertTrue(promoted);
        assertEquals(102, promotions.getFirst().scoreAtPromotion());
    }

    @Test
    void 작성_후_48시간이_지나면_점수가_높아도_승격하지_않는다() {
        List<PopularPostPromotion> promotions = new ArrayList<>();
        PopularitySnapshot snapshot = new PopularitySnapshot(
                1, 10, LocalDateTime.ofInstant(NOW, ZONE).minusHours(48), 100, 100, false);

        boolean promoted = processor(promotions).evaluate(snapshot);

        assertFalse(promoted);
        assertTrue(promotions.isEmpty());
    }

    private PopularityPromotionProcessor processor(List<PopularPostPromotion> promotions) {
        return new PopularityPromotionProcessor(
                promotions::add,
                new PopularityProperties(3, 2, 100, Duration.ofHours(48), 100),
                Clock.fixed(NOW, ZONE)
        );
    }
}
