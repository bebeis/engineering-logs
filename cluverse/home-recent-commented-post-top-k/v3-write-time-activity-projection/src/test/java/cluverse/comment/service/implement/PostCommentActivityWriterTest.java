package cluverse.comment.service.implement;

import cluverse.comment.domain.Comment;
import cluverse.comment.domain.PostCommentActivity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostCommentActivityWriterTest {

    @Test
    void 최신_댓글을_삭제하면_flush_후_다음_댓글로_교체한다() {
        Comment older = new Comment(1, 10, LocalDateTime.of(2026, 5, 29, 9, 0));
        Comment latest = new Comment(2, 10, LocalDateTime.of(2026, 5, 29, 10, 0));
        List<String> calls = new ArrayList<>();
        RecordingCommentStore comments = new RecordingCommentStore(older, calls);
        RecordingActivityStore activities = new RecordingActivityStore(
                new PostCommentActivity(10, latest.latestKey()), calls);
        PostCommentActivityWriter writer = new PostCommentActivityWriter(activities, comments);

        writer.reflectDeleted(10, latest.id());

        assertEquals(older.id(), activities.activity.latest().commentId());
        assertEquals(List.of("lock", "flush", "read-latest", "save"), calls);
    }

    @Test
    void 마지막_댓글을_삭제하면_활동_행도_삭제한다() {
        Comment latest = new Comment(2, 10, LocalDateTime.of(2026, 5, 29, 10, 0));
        List<String> calls = new ArrayList<>();
        RecordingCommentStore comments = new RecordingCommentStore(null, calls);
        RecordingActivityStore activities = new RecordingActivityStore(
                new PostCommentActivity(10, latest.latestKey()), calls);

        new PostCommentActivityWriter(activities, comments).reflectDeleted(10, latest.id());

        assertTrue(activities.removed);
    }

    private static final class RecordingCommentStore implements CommentStore {
        private final Comment next;
        private final List<String> calls;
        private RecordingCommentStore(Comment next, List<String> calls) {
            this.next = next;
            this.calls = calls;
        }
        public Comment insert(long memberId, long postId, String content, LocalDateTime at) { return null; }
        public Comment lock(long commentId) { return null; }
        public void markDeleted(Comment comment) { }
        public void flush() { calls.add("flush"); }
        public Optional<Comment> readLatestVisible(long postId) {
            calls.add("read-latest");
            return Optional.ofNullable(next);
        }
    }

    private static final class RecordingActivityStore implements PostCommentActivityStore {
        private final List<String> calls;
        private final PostCommentActivity activity;
        private boolean removed;
        private RecordingActivityStore(PostCommentActivity activity, List<String> calls) {
            this.activity = activity;
            this.calls = calls;
        }
        public void upsertLatest(Comment comment) { }
        public Optional<PostCommentActivity> lock(long postId) { calls.add("lock"); return Optional.of(activity); }
        public void save(PostCommentActivity activity) { calls.add("save"); }
        public void remove(PostCommentActivity activity) { removed = true; }
    }
}
