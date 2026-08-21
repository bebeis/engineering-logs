package cluverse.popularity.domain;

import java.time.LocalDateTime;

public record StoredPopularityPolicy(
        long boardId,
        PopularityPolicy policy,
        int sampleSize,
        String source,
        LocalDateTime computedAt
) {
}
