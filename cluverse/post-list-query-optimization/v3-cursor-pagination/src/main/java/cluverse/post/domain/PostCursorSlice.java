package cluverse.post.domain;

import java.util.List;

public record PostCursorSlice(
        List<PostSummary> posts,
        boolean hasMore
) {
    public PostCursorSlice {
        posts = List.copyOf(posts);
    }
}
