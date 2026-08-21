package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

public interface CommentStore {
    Comment read(long commentId);
    boolean hasChildren(long postId, long commentId);
    void update(Comment comment);
    void remove(Comment comment);
}
