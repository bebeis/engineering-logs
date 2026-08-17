package cluverse.post.domain;

import java.time.LocalDateTime;

public record PostSummary(
        Long postId,
        String title,
        String contentPreview,
        String thumbnailUrl,
        long viewCount,
        long likeCount,
        long commentCount,
        String authorNickname,
        LocalDateTime createdAt
) {
}
