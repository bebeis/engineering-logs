package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

import java.util.List;

public interface PersistedPathCommentReader {
    List<Comment> readThread(long postId, String rootPath, String afterPath, int limitPlusOne);
}
