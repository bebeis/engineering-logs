package cluverse.popularity.service.implement;

import java.time.Duration;

public record PopularityProperties(
        int likeWeight,
        int commentWeight,
        long defaultPromotionScore,
        Duration promotionWindow,
        int scanChunkSize
) {
    public PopularityProperties {
        if (likeWeight < 0 || commentWeight < 0 || defaultPromotionScore < 0) {
            throw new IllegalArgumentException("점수 설정은 음수일 수 없습니다.");
        }
        if (promotionWindow.isNegative() || promotionWindow.isZero() || scanChunkSize < 1) {
            throw new IllegalArgumentException("승격 기간과 스캔 크기는 양수여야 합니다.");
        }
    }
}
