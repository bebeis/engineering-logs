package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;

import java.time.Clock;
import java.time.LocalDateTime;

public class CommentPathWriteProcessor {
    private static final int MAX_DEPTH = 5;

    private final CommentPathStore store;
    private final Clock clock;

    public CommentPathWriteProcessor(CommentPathStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    public Comment create(long memberId, long postId, Long parentId, String content) {
        Comment parent = parentId == null ? null : store.lock(parentId)
                .orElseThrow(() -> new IllegalStateException("부모 댓글이 없습니다."));
        int depth = parent == null ? 0 : parent.depth() + 1;
        if (depth > MAX_DEPTH) {
            throw new IllegalStateException("최대 댓글 깊이를 초과했습니다.");
        }

        Comment saved = store.insert(postId, memberId, parentId, depth, content, now());
        saved.assignPath(parent);
        store.updatePath(saved);
        return saved;
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), clock.getZone());
    }
}
