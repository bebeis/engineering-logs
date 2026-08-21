package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;
import cluverse.comment.domain.PostCommentActivity;

public class PostCommentActivityWriter {
    private final PostCommentActivityStore activityStore;
    private final CommentStore commentStore;

    public PostCommentActivityWriter(
            PostCommentActivityStore activityStore,
            CommentStore commentStore
    ) {
        this.activityStore = activityStore;
        this.commentStore = commentStore;
    }

    public void reflectCreated(Comment comment) {
        activityStore.upsertLatest(comment);
    }

    public void reflectDeleted(long postId, long deletedCommentId) {
        activityStore.lock(postId)
                .filter(activity -> activity.latest().commentId() == deletedCommentId)
                .ifPresent(this::replaceOrRemove);
    }

    private void replaceOrRemove(PostCommentActivity activity) {
        commentStore.flush();
        commentStore.readLatestVisible(activity.postId()).ifPresentOrElse(
                latest -> {
                    activity.replaceLatest(latest);
                    activityStore.save(activity);
                },
                () -> activityStore.remove(activity)
        );
    }
}
