package cluverse.popularity.service;

import cluverse.popularity.domain.PopularityTrigger;
import cluverse.popularity.service.implement.PopularityPolicyStore;
import cluverse.popularity.service.implement.PopularityPromotionProcessor;
import cluverse.popularity.service.implement.PopularityProperties;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PopularityPromotionServiceTest {

    @Test
    void 승격_실패를_좋아요와_댓글_요청으로_전파하지_않는다() {
        List<RuntimeException> reported = new ArrayList<>();
        PopularityPromotionProcessor failingProcessor = new PopularityPromotionProcessor(
                postId -> { throw new IllegalStateException("snapshot failure"); },
                new PopularityPolicyStore(new EmptyPolicyRepository(), properties()),
                new NoOpPopularPostWriter(),
                properties(),
                Clock.systemUTC()
        );
        PopularityPromotionService service = new PopularityPromotionService(failingProcessor, reported::add);

        assertDoesNotThrow(() -> service.tryPromote(1, PopularityTrigger.LIKE));
        assertEquals(1, reported.size());
    }

    private PopularityProperties properties() {
        return new PopularityProperties(
                3, 2, 100, Duration.ofHours(48), Duration.ofDays(7),
                0.98, 3, 0.3, 100
        );
    }
}
