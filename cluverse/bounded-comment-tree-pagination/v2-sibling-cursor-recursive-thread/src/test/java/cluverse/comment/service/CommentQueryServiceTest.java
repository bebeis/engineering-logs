package cluverse.comment.service;

import cluverse.comment.domain.Comment;
import cluverse.comment.domain.CommentCursor;
import cluverse.comment.service.implement.CommentReader;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentQueryServiceTest {

    @Test
    void 직계_자식은_limit_더하기_1만_읽고_ID로_동일시각_순서를_고정한다() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 26, 10, 0);
        CommentReader reader = new CommentReader() {
            public long readMaxCommentId() { return 3; }
            public List<Comment> readDirectChildren(
                    long postId, Long parentId, CommentCursor cursor, int limitPlusOne
            ) {
                assertEquals(3, limitPlusOne);
                return List.of(comment(1, createdAt), comment(2, createdAt), comment(3, createdAt));
            }
            public List<Comment> readThread(long postId, long rootId, String path, int limit) {
                return List.of();
            }
        };
        CommentQueryService service = new CommentQueryService(
                reader, Clock.fixed(Instant.parse("2026-05-26T01:00:00Z"), ZoneId.of("Asia/Seoul")));

        CommentPage page = service.readChildren(10, null, null, 2);

        assertEquals(List.of(1L, 2L), page.comments().stream().map(Comment::id).toList());
        assertTrue(page.hasNext());
        assertEquals(2, page.nextCursor().lastCommentId());
    }

    private Comment comment(long id, LocalDateTime createdAt) {
        return new Comment(id, 10, 1, null, 0, createdAt);
    }
}
