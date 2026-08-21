package cluverse.home.domain;

import java.time.LocalDateTime;

public record RecentCommentedPost(long postId, String title, LocalDateTime lastCommentedAt) {
}
