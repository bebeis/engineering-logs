package cluverse.comment.domain;

import java.time.LocalDateTime;

public record CommentView(
        long commentId,
        Long parentId,
        int depth,
        String content,
        LocalDateTime createdAt
) {
}
