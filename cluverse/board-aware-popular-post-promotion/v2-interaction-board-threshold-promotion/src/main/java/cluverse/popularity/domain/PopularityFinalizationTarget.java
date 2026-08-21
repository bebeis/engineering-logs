package cluverse.popularity.domain;

import java.time.LocalDateTime;

public record PopularityFinalizationTarget(
        long popularPostId,
        long postId,
        LocalDateTime finalizeAt
) {
}
