package cluverse.post.service.response;

import cluverse.post.domain.PostSummary;

import java.time.LocalDateTime;
import java.util.List;

public record PostCursorPageResponse(
        List<PostItemResponse> posts,
        int size,
        boolean hasNext,
        boolean hasPrev,
        Cursor prevCursor,
        Cursor nextCursor
) {
    public PostCursorPageResponse {
        posts = List.copyOf(posts);
    }

    public static PostCursorPageResponse from(
            List<PostSummary> posts,
            int size,
            boolean hasNext,
            boolean hasPrev,
            Cursor prevCursor,
            Cursor nextCursor
    ) {
        return new PostCursorPageResponse(
                posts.stream().map(PostItemResponse::from).toList(),
                size,
                hasNext,
                hasPrev,
                prevCursor,
                nextCursor
        );
    }

    public record Cursor(LocalDateTime createdAt, Long postId) {
    }

    public record PostItemResponse(
            Long postId,
            String title,
            String contentPreview,
            LocalDateTime createdAt
    ) {
        private static PostItemResponse from(PostSummary post) {
            return new PostItemResponse(
                    post.postId(),
                    post.title(),
                    post.contentPreview(),
                    post.createdAt()
            );
        }
    }
}
