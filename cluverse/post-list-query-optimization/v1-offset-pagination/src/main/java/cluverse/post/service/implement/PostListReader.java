package cluverse.post.service.implement;

import cluverse.post.domain.PostListSnapshot;
import cluverse.post.repository.PostListQueryRepository;
import cluverse.post.service.request.PostListRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostListReader {

    private final PostListQueryRepository postListQueryRepository;

    public PostListSnapshot read(PostListRequest request) {
        return new PostListSnapshot(
                postListQueryRepository.findSummariesWithOffset(request),
                postListQueryRepository.countActivePosts(request.boardId())
        );
    }
}
