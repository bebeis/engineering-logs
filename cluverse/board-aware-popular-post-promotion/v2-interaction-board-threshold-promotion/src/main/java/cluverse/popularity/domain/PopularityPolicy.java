package cluverse.popularity.domain;

public record PopularityPolicy(long promotionScore) {
    public PopularityPolicy {
        if (promotionScore < 0) {
            throw new IllegalArgumentException("승격 기준은 음수일 수 없습니다.");
        }
    }
}
