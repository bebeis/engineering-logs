package cluverse.comment.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record LatestCommentKey(LocalDateTime commentedAt, long commentId)
        implements Comparable<LatestCommentKey> {

    public LatestCommentKey {
        commentedAt = commentedAt.truncatedTo(ChronoUnit.SECONDS);
    }

    @Override
    public int compareTo(LatestCommentKey other) {
        int byTime = commentedAt.compareTo(other.commentedAt);
        return byTime != 0 ? byTime : Long.compare(commentId, other.commentId);
    }
}
