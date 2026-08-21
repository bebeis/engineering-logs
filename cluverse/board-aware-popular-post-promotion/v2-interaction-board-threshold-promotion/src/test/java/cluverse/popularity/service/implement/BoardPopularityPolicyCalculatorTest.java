package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularityPolicy;
import cluverse.popularity.domain.PopularityPolicySample;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardPopularityPolicyCalculatorTest {

    @Test
    void 승격된_글은_전체_노출_전_점수로_분포에_참여한다() {
        BoardPopularityPolicyCalculator calculator = new BoardPopularityPolicyCalculator(properties());
        List<PopularityPolicySample> samples = List.of(
                new PopularityPolicySample(2, 0, null),
                new PopularityPolicySample(100, 100, 10L),
                new PopularityPolicySample(4, 0, null)
        );

        var result = calculator.calculate(samples, Optional.empty());

        assertEquals(12, result.policy().promotionScore());
        assertEquals("DISTRIBUTION", result.source());
    }

    @Test
    void 새_기준은_기존_기준과_스무딩한다() {
        BoardPopularityPolicyCalculator calculator = new BoardPopularityPolicyCalculator(properties());
        List<PopularityPolicySample> samples = List.of(
                new PopularityPolicySample(10, 0, null),
                new PopularityPolicySample(20, 0, null),
                new PopularityPolicySample(30, 0, null)
        );

        var result = calculator.calculate(samples, Optional.of(new PopularityPolicy(60)));

        assertEquals(69, result.policy().promotionScore());
    }

    private PopularityProperties properties() {
        return new PopularityProperties(
                3, 2, 100, Duration.ofHours(48), Duration.ofDays(7),
                0.98, 3, 0.3, 100
        );
    }
}
