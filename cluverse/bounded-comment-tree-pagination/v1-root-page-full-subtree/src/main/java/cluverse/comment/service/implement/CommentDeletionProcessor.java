package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

public class CommentDeletionProcessor {
    private final CommentStore store;

    public CommentDeletionProcessor(CommentStore store) {
        this.store = store;
    }

    public void delete(long memberId, long postId, long commentId) {
        Comment comment = store.read(commentId);
        if (comment.authorId() != memberId) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }
        if (store.hasChildren(postId, commentId)) {
            comment.deleteSoftly();
            store.update(comment);
            return;
        }
        store.remove(comment);
    }
}
