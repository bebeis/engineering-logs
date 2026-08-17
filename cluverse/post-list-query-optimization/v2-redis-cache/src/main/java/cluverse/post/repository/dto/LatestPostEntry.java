package cluverse.post.repository.dto;

import java.time.LocalDateTime;

public record LatestPostEntry(
        Long postId,
        LocalDateTime createdAt
) {
}
