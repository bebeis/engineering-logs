package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

import java.time.Clock;
import java.time.LocalDateTime;

public class CommentProcessor {
    private final CommentStore commentStore;
    private final PostCommentActivityWriter activityWriter;
    private final Clock clock;

    public CommentProcessor(
            CommentStore commentStore,
            PostCommentActivityWriter activityWriter,
            Clock clock
    ) {
        this.commentStore = commentStore;
        this.activityWriter = activityWriter;
        this.clock = clock;
    }

    public Comment create(long memberId, long postId, String content) {
        Comment comment = commentStore.insert(memberId, postId, content, now());
        activityWriter.reflectCreated(comment);
        return comment;
    }

    public void delete(long commentId) {
        Comment comment = commentStore.lock(commentId);
        comment.delete();
        commentStore.markDeleted(comment);
        activityWriter.reflectDeleted(comment.postId(), comment.id());
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), clock.getZone());
    }
}
