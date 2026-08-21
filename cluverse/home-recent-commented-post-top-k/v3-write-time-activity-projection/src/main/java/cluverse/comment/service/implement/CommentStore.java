package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommentStore {
    Comment insert(long memberId, long postId, String content, LocalDateTime createdAt);
    Comment lock(long commentId);
    void markDeleted(Comment comment);
    void flush();
    Optional<Comment> readLatestVisible(long postId);
}
