package cluverse.post.repository.dto;

import java.util.List;

public record PostIdSlice(
        List<Long> postIds,
        boolean hasMore
) {
    public PostIdSlice {
        postIds = List.copyOf(postIds);
    }
}
