package cluverse.comment.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LatestCommentKeyTest {

    @Test
    void 생성시각을_먼저_비교하고_같은_초에는_commentId를_비교한다() {
        LocalDateTime time = LocalDateTime.of(2026, 5, 29, 10, 0);
        LatestCommentKey first = new LatestCommentKey(time, 10);
        LatestCommentKey sameSecondLaterId = new LatestCommentKey(time.plusNanos(500_000_000), 11);
        LatestCommentKey olderButHigherId = new LatestCommentKey(time.minusSeconds(1), 100);

        assertTrue(sameSecondLaterId.compareTo(first) > 0);
        assertTrue(olderButHigherId.compareTo(first) < 0);
    }
}
