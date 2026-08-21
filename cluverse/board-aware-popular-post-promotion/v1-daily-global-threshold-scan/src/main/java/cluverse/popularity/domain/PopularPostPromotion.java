package cluverse.popularity.domain;

import java.time.LocalDateTime;

public record PopularPostPromotion(
        String algorithmVersion,
        long postId,
        long boardId,
        LocalDateTime promotedAt,
        LocalDateTime finalizeAt,
        long scoreAtPromotion,
        long promotionScoreThreshold
) {
}
