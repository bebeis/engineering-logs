package cluverse.comment.service;

import cluverse.comment.domain.Comment;
import cluverse.comment.domain.CommentCursor;
import cluverse.comment.service.implement.CommentReader;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class CommentQueryService {
    private final CommentReader reader;
    private final Clock clock;

    public CommentQueryService(CommentReader reader, Clock clock) {
        this.reader = reader;
        this.clock = clock;
    }

    public CommentPage readChildren(long postId, Long parentId, CommentCursor cursor, int limit) {
        CommentCursor resolved = cursor == null
                ? CommentCursor.first(LocalDateTime.ofInstant(clock.instant(), clock.getZone()), reader.readMaxCommentId())
                : cursor;
        List<Comment> selected = reader.readDirectChildren(postId, parentId, resolved, limit + 1);
        boolean hasNext = selected.size() > limit;
        List<Comment> content = hasNext ? selected.subList(0, limit) : selected;
        CommentCursor next = hasNext ? nextCursor(content.getLast(), resolved) : null;
        return new CommentPage(content, next, hasNext);
    }

    public List<Comment> readThread(long postId, long rootCommentId, String afterSortPath, int limit) {
        List<Comment> selected = reader.readThread(postId, rootCommentId, afterSortPath, limit + 1);
        return selected.size() > limit ? selected.subList(0, limit) : selected;
    }

    private CommentCursor nextCursor(Comment last, CommentCursor previous) {
        return new CommentCursor(
                last.createdAt(), last.id(), previous.asOf(), previous.snapshotMaxCommentId());
    }
}
