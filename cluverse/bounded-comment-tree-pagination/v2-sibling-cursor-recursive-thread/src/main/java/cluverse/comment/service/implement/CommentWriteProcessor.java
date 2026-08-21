package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

import java.time.Clock;
import java.time.LocalDateTime;

public class CommentWriteProcessor {
    private static final int MAX_DEPTH = 5;

    private final CommentStore store;
    private final Clock clock;

    public CommentWriteProcessor(CommentStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    public Comment createReply(long memberId, long postId, long parentId, String content) {
        Comment parent = store.lock(parentId).orElseThrow(() -> new IllegalStateException("부모 댓글이 없습니다."));
        if (parent.postId() != postId || !parent.active()) {
            throw new IllegalStateException("답글을 작성할 수 없는 부모 댓글입니다.");
        }
        int depth = parent.depth() + 1;
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException("최대 댓글 깊이를 초과했습니다.");
        }
        return store.insert(postId, memberId, parentId, depth, content, now());
    }

    public void delete(long memberId, long commentId) {
        Comment comment = store.lock(commentId)
                .orElseThrow(() -> new IllegalStateException("댓글이 없습니다."));
        if (comment.authorId() != memberId) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }
        if (store.hasChildren(comment.postId(), comment.id())) {
            comment.deleteSoftly();
            store.update(comment);
            return;
        }
        store.remove(comment);
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), clock.getZone());
    }
}
