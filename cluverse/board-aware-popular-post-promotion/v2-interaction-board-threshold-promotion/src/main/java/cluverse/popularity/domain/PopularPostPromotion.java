package cluverse.popularity.domain;

import java.time.LocalDateTime;

public record PopularPostPromotion(
        long postId,
        long boardId,
        LocalDateTime promotedAt,
        LocalDateTime finalizeAt,
        long scoreAtPromotion,
        long likeCountAtPromotion,
        long commentCountAtPromotion,
        long promotionScoreThreshold,
        PopularityTrigger trigger
) {
}
