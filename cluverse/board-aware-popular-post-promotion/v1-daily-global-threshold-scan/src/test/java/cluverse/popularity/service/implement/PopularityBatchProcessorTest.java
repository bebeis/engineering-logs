package cluverse.popularity.service.implement;

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

class PopularityBatchProcessorTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Instant NOW = Instant.parse("2026-05-25T01:00:00Z");

    @Test
    void 최근_게시글을_작성시각과_ID_키셋으로_끝까지_순회한다() {
        LocalDateTime createdFrom = LocalDateTime.ofInstant(NOW, ZONE).minusHours(48);
        PopularitySnapshot first = snapshot(1, createdFrom.plusMinutes(1));
        PopularitySnapshot second = snapshot(2, createdFrom.plusMinutes(2));
        PopularitySnapshot third = snapshot(3, createdFrom.plusMinutes(3));
        List<Long> evaluated = new ArrayList<>();
        PopularitySnapshotReader reader = (from, lastCreatedAt, lastPostId, limit) -> {
            if (lastPostId == 0) return List.of(first, second);
            if (lastPostId == 2) return List.of(third);
            return List.of();
        };
        PopularityProperties properties = properties();
        PopularityPromotionProcessor evaluator = new PopularityPromotionProcessor(
                promotion -> evaluated.add(promotion.postId()), properties, Clock.fixed(NOW, ZONE));
        PopularityBatchProcessor processor = new PopularityBatchProcessor(
                reader, evaluator, properties, Clock.fixed(NOW, ZONE));

        int examined = processor.runDaily();

        assertEquals(3, examined);
        assertEquals(List.of(1L, 2L, 3L), evaluated);
    }

    private PopularitySnapshot snapshot(long postId, LocalDateTime createdAt) {
        return new PopularitySnapshot(postId, 10, createdAt, 40, 0, false);
    }

    private PopularityProperties properties() {
        return new PopularityProperties(3, 2, 100, Duration.ofHours(48), 2);
    }
}
