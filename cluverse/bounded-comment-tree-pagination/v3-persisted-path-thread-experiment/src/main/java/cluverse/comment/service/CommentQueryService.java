package cluverse.comment.service;

import cluverse.comment.domain.Comment;
import cluverse.comment.service.implement.PersistedPathCommentReader;

import java.util.List;

public class CommentQueryService {
    private final PersistedPathCommentReader reader;

    public CommentQueryService(PersistedPathCommentReader reader) {
        this.reader = reader;
    }

    public CommentPage readThread(long postId, String rootPath, String afterPath, int limit) {
        List<Comment> selected = reader.readThread(postId, rootPath, afterPath, limit + 1);
        boolean hasNext = selected.size() > limit;
        List<Comment> content = hasNext ? selected.subList(0, limit) : selected;
        String nextPath = hasNext ? content.getLast().path() : null;
        return new CommentPage(content, nextPath, hasNext);
    }
}
