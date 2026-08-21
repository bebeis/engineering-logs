package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommentPathStore {
    Optional<Comment> lock(long commentId);
    Comment insert(long postId, long authorId, Long parentId, int depth, String content, LocalDateTime createdAt);
    void updatePath(Comment comment);
}
