package cluverse.post.service.implement;

import cluverse.post.service.request.PostListRequest;

import java.time.Duration;

public record PostListCachePolicy(
        boolean enabled,
        int maxEntries,
        Duration ttl,
        Duration lockLease
) {
    public static PostListCachePolicy defaults() {
        return new PostListCachePolicy(true, 201, Duration.ofMinutes(3), Duration.ofSeconds(2));
    }

    public boolean supports(PostListRequest request, long searchLimit) {
        return enabled
                && request.lastRequiredIndex() < maxEntries
                && searchLimit <= maxEntries;
    }
}
