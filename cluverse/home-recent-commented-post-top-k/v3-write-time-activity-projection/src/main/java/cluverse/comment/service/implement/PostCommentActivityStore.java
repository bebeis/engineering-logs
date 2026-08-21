package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;
import cluverse.comment.domain.PostCommentActivity;

import java.util.Optional;

public interface PostCommentActivityStore {
    void upsertLatest(Comment comment);
    Optional<PostCommentActivity> lock(long postId);
    void save(PostCommentActivity activity);
    void remove(PostCommentActivity activity);
}
