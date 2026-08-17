package cluverse.post.service;

import cluverse.post.domain.PostCursorSlice;
import cluverse.post.domain.PostSummary;
import cluverse.post.service.implement.PostListReader;
import cluverse.post.service.request.PostCursorDirection;
import cluverse.post.service.request.PostCursorRequest;
import cluverse.post.service.response.PostCursorPageResponse;
import cluverse.post.service.response.PostCursorPageResponse.Cursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostListQueryService {

    private final PostListReader postListReader;

    public PostCursorPageResponse getPosts(PostCursorRequest request) {
        PostCursorSlice slice = postListReader.read(request);
        List<PostSummary> posts = slice.posts();

        return PostCursorPageResponse.from(
                posts,
                request.sizeOrDefault(),
                resolveHasNext(request, slice),
                resolveHasPrev(request, slice),
                toCursor(firstOf(posts)),
                toCursor(lastOf(posts))
        );
    }

    private boolean resolveHasNext(PostCursorRequest request, PostCursorSlice slice) {
        return request.isPreviousMove() || slice.hasMore();
    }

    private boolean resolveHasPrev(PostCursorRequest request, PostCursorSlice slice) {
        if (request.hasCursor()) {
            return request.directionOrDefault() == PostCursorDirection.PREV
                    ? slice.hasMore()
                    : true;
        }
        return request.isDateAnchored() && postListReader.existsNewerThan(request);
    }

    private Cursor toCursor(PostSummary post) {
        return post == null ? null : new Cursor(post.createdAt(), post.postId());
    }

    private PostSummary firstOf(List<PostSummary> posts) {
        return posts.isEmpty() ? null : posts.getFirst();
    }

    private PostSummary lastOf(List<PostSummary> posts) {
        return posts.isEmpty() ? null : posts.getLast();
    }
}
