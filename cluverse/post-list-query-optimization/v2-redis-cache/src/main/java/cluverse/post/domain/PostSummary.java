package cluverse.post.domain;

import java.time.LocalDateTime;

public record PostSummary(
        Long postId,
        String title,
        String contentPreview,
        long viewCount,
        LocalDateTime createdAt
) {
}
