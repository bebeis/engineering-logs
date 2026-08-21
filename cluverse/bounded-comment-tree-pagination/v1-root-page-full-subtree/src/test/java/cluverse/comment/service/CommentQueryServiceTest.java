package cluverse.comment.service;

import cluverse.comment.domain.CommentView;
import cluverse.comment.service.implement.CommentReader;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentQueryServiceTest {

    @Test
    void 루트_limit은_전체_응답_댓글수를_제한하지_못한다() {
        List<CommentView> subtree = LongStream.rangeClosed(1, 50_001)
                .mapToObj(id -> new CommentView(id, id == 1 ? null : 1L, id == 1 ? 0 : 1,
                        "comment", LocalDateTime.of(2026, 5, 26, 10, 0)))
                .toList();
        CommentReader reader = new CommentReader() {
            public List<Long> readRootIds(long postId, int offset, int limit) { return List.of(1L); }
            public List<CommentView> readWholeSubtrees(long viewerId, List<Long> roots, int depth) {
                return subtree;
            }
        };

        CommentPage page = new CommentQueryService(reader).read(1, 10, 0, 20);

        assertEquals(20, page.limit());
        assertEquals(50_001, page.comments().size());
    }
}
