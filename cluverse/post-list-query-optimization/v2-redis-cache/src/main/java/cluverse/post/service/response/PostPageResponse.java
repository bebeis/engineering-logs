package cluverse.post.service.response;

import cluverse.post.domain.PostSummary;

import java.time.LocalDateTime;
import java.util.List;

public record PostPageResponse(
        List<PostItemResponse> posts,
        int page,
        int size,
        boolean hasNext,
        int lastPage,
        boolean hasNextBlock
) {
    public PostPageResponse {
        posts = List.copyOf(posts);
    }

    public static PostPageResponse from(
            List<PostSummary> posts,
            int page,
            int size,
            boolean hasNext,
            int lastPage,
            boolean hasNextBlock
    ) {
        return new PostPageResponse(
                posts.stream().map(PostItemResponse::from).toList(),
                page,
                size,
                hasNext,
                lastPage,
                hasNextBlock
        );
    }

    public record PostItemResponse(
            Long postId,
            String title,
            String contentPreview,
            long viewCount,
            LocalDateTime createdAt
    ) {
        private static PostItemResponse from(PostSummary post) {
            return new PostItemResponse(
                    post.postId(),
                    post.title(),
                    post.contentPreview(),
                    post.viewCount(),
                    post.createdAt()
            );
        }
    }
}
