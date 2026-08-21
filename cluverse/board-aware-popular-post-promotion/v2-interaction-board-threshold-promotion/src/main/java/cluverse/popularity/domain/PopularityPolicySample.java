package cluverse.popularity.domain;

public record PopularityPolicySample(
        long likeCount,
        long commentCount,
        Long scoreAtPromotion
) {
}
