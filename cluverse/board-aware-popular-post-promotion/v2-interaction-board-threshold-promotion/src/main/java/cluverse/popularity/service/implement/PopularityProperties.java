package cluverse.popularity.service.implement;

import java.time.Duration;

public record PopularityProperties(
        int likeWeight,
        int commentWeight,
        long defaultPromotionScore,
        Duration promotionWindow,
        Duration policySampleWindow,
        double policyPercentile,
        int policyMinSampleSize,
        double policySmoothingRatio,
        int finalizationBatchSize
) {
    public PopularityProperties {
        if (likeWeight < 0 || commentWeight < 0 || defaultPromotionScore < 0) {
            throw new IllegalArgumentException("점수 설정은 음수일 수 없습니다.");
        }
        if (policyPercentile <= 0 || policyPercentile > 1) {
            throw new IllegalArgumentException("분위수는 0보다 크고 1 이하여야 합니다.");
        }
        if (policySmoothingRatio < 0 || policySmoothingRatio > 1) {
            throw new IllegalArgumentException("스무딩 비율은 0 이상 1 이하여야 합니다.");
        }
        if (policyMinSampleSize < 1 || finalizationBatchSize < 1) {
            throw new IllegalArgumentException("표본 수와 배치 크기는 양수여야 합니다.");
        }
    }
}
