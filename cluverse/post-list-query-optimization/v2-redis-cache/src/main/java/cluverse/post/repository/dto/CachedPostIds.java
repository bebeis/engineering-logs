package cluverse.post.repository.dto;

import java.util.List;

public record CachedPostIds(
        List<Long> postIds,
        long cachedCount
) {
    public CachedPostIds {
        postIds = List.copyOf(postIds);
    }
}
