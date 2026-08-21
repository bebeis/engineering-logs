package cluverse.home.domain;

import java.time.LocalDateTime;

public record RecentCommentCandidate(long postId, LocalDateTime lastCommentedAt) {
}
