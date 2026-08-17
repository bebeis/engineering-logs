package cluverse.post.domain;

import java.util.List;

public record PostListSnapshot(
        List<PostSummary> posts,
        long totalCount
) {
    public PostListSnapshot {
        posts = List.copyOf(posts);
    }
}
