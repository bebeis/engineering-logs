package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularityPolicy;
import cluverse.popularity.domain.PopularityPolicySample;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BoardPopularityPolicyCalculator {

    private final PopularityProperties properties;

    public BoardPopularityPolicyCalculator(PopularityProperties properties) {
        this.properties = properties;
    }

    public Calculation calculate(
            List<PopularityPolicySample> samples,
            Optional<PopularityPolicy> previous
    ) {
        if (samples.size() < properties.policyMinSampleSize()) {
            return new Calculation(
                    smooth(previous, new PopularityPolicy(properties.defaultPromotionScore())),
                    "DEFAULT"
            );
        }

        List<Long> scores = samples.stream()
                .map(this::sampleScore)
                .sorted(Comparator.naturalOrder())
                .toList();
        int index = Math.max(0, (int) Math.ceil(properties.policyPercentile() * scores.size()) - 1);
        PopularityPolicy calculated = new PopularityPolicy(scores.get(index));
        return new Calculation(smooth(previous, calculated), "DISTRIBUTION");
    }

    private long sampleScore(PopularityPolicySample sample) {
        if (sample.scoreAtPromotion() != null) {
            return sample.scoreAtPromotion();
        }
        return new PopularityScore(properties.likeWeight(), properties.commentWeight())
                .calculate(sample.likeCount(), sample.commentCount());
    }

    private PopularityPolicy smooth(
            Optional<PopularityPolicy> previous,
            PopularityPolicy calculated
    ) {
        return previous.map(old -> new PopularityPolicy(Math.round(
                        old.promotionScore() * (1 - properties.policySmoothingRatio())
                                + calculated.promotionScore() * properties.policySmoothingRatio()
                )))
                .orElse(calculated);
    }

    public record Calculation(PopularityPolicy policy, String source) {
    }
}
