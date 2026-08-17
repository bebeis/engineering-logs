package cluverse.post.domain;

import java.util.List;

public record PostPageData(
        List<PostSummary> posts,
        boolean hasNext,
        long cappedCount
) {
    public PostPageData {
        posts = List.copyOf(posts);
    }
}
