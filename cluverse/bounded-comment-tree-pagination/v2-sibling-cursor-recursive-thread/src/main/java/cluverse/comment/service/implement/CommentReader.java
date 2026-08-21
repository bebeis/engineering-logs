package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;
import cluverse.comment.domain.CommentCursor;

import java.util.List;

public interface CommentReader {
    long readMaxCommentId();
    List<Comment> readDirectChildren(long postId, Long parentId, CommentCursor cursor, int limitPlusOne);
    List<Comment> readThread(long postId, long rootCommentId, String afterSortPath, int limitPlusOne);
}
