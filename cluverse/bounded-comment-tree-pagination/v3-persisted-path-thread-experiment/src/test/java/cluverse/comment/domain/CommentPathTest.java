package cluverse.comment.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentPathTest {

    @Test
    void path는_부모_prefix와_형제_정렬키를_누적한다() {
        LocalDateTime time = LocalDateTime.of(2026, 5, 26, 10, 0);
        Comment root = new Comment(1, 10, null, 0, time);
        root.assignPath(null);
        Comment first = new Comment(2, 10, 1L, 1, time);
        first.assignPath(root);
        Comment second = new Comment(3, 10, 1L, 1, time);
        second.assignPath(root);

        assertTrue(first.path().startsWith(root.path() + "/"));
        assertTrue(first.path().compareTo(second.path()) < 0);
        assertTrue(second.path().length() <= Comment.MAX_PATH_LENGTH);
    }
}
