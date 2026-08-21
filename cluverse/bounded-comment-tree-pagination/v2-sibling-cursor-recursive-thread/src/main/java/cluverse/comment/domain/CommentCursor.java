package cluverse.comment.domain;

import java.time.LocalDateTime;

public record CommentCursor(
        LocalDateTime lastCreatedAt,
        long lastCommentId,
        LocalDateTime asOf,
        long snapshotMaxCommentId
) {
    public CommentCursor {
        if (lastCreatedAt == null || asOf == null || lastCommentId < 0 || snapshotMaxCommentId < 0) {
            throw new IllegalArgumentException("유효하지 않은 댓글 커서입니다.");
        }
    }

    public static CommentCursor first(LocalDateTime asOf, long snapshotMaxCommentId) {
        return new CommentCursor(LocalDateTime.MIN, 0, asOf, snapshotMaxCommentId);
    }
}
