package cluverse.popularity.domain;

import java.time.LocalDateTime;

public record PopularitySnapshot(
        long postId,
        long boardId,
        LocalDateTime createdAt,
        long likeCount,
        long commentCount,
        boolean deleted
) {
}
