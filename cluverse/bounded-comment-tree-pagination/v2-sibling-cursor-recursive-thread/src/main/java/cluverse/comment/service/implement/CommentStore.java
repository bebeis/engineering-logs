package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CommentStore {
    Optional<Comment> lock(long commentId);
    Comment insert(long postId, long authorId, Long parentId, int depth, String content, LocalDateTime createdAt);
    boolean hasChildren(long postId, long commentId);
    void update(Comment comment);
    void remove(Comment comment);
}
