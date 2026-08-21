package cluverse.comment.service;

import cluverse.comment.domain.CommentView;
import cluverse.comment.service.implement.CommentReader;

import java.util.List;

public class CommentQueryService {

    private static final int MAX_DEPTH = 5;

    private final CommentReader commentReader;

    public CommentQueryService(CommentReader commentReader) {
        this.commentReader = commentReader;
    }

    public CommentPage read(long viewerId, long postId, int offset, int limit) {
        List<Long> selected = commentReader.readRootIds(postId, offset, limit + 1);
        boolean hasNext = selected.size() > limit;
        List<Long> rootIds = hasNext ? selected.subList(0, limit) : selected;
        List<CommentView> wholeTrees = commentReader.readWholeSubtrees(viewerId, rootIds, MAX_DEPTH);
        return new CommentPage(wholeTrees, offset, limit, hasNext);
    }
}
