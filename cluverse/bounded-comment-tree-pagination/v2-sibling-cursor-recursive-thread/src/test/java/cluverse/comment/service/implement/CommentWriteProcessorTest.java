package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentWriteProcessorTest {

    @Test
    void 답글_작성과_삭제는_같은_댓글을_먼저_잠근다() {
        RecordingStore store = new RecordingStore(new Comment(
                100, 10, 1, null, 0, LocalDateTime.of(2026, 5, 26, 10, 0)));
        CommentWriteProcessor processor = new CommentWriteProcessor(
                store, Clock.fixed(Instant.parse("2026-05-26T01:00:00Z"), ZoneOffset.UTC));

        processor.createReply(2, 10, 100, "reply");
        processor.delete(1, 100);

        assertEquals(List.of("lock-100", "insert-100", "lock-100", "soft-delete-100"), store.calls);
        assertTrue(store.parent.deleted());
    }

    private static final class RecordingStore implements CommentStore {
        private final Comment parent;
        private final List<String> calls = new ArrayList<>();
        private boolean childExists;

        private RecordingStore(Comment parent) { this.parent = parent; }

        public Optional<Comment> lock(long commentId) {
            calls.add("lock-" + commentId);
            return Optional.of(parent);
        }
        public Comment insert(long postId, long authorId, Long parentId, int depth,
                              String content, LocalDateTime createdAt) {
            calls.add("insert-" + parentId);
            childExists = true;
            return new Comment(101, postId, authorId, parentId, depth, createdAt);
        }
        public boolean hasChildren(long postId, long commentId) { return childExists; }
        public void update(Comment comment) { calls.add("soft-delete-" + comment.id()); }
        public void remove(Comment comment) { calls.add("physical-delete-" + comment.id()); }
    }
}
