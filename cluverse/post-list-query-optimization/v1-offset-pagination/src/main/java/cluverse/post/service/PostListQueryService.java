package cluverse.post.service;

import cluverse.post.domain.PostListSnapshot;
import cluverse.post.service.implement.PostListReader;
import cluverse.post.service.request.PostListRequest;
import cluverse.post.service.response.PostPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostListQueryService {

    private static final int PAGE_BLOCK_SIZE = 10;

    private final PostListReader postListReader;

    public PostPageResponse getPosts(PostListRequest request) {
        PostListSnapshot snapshot = postListReader.read(request);

        int page = request.pageOrDefault();
        int size = request.sizeOrDefault();
        int actualLastPage = (int) Math.max(1, (snapshot.totalCount() + size - 1) / size);
        int blockEndPage = ((page - 1) / PAGE_BLOCK_SIZE + 1) * PAGE_BLOCK_SIZE;
        boolean hasNextBlock = actualLastPage > blockEndPage;

        return PostPageResponse.from(
                snapshot.posts(),
                page,
                size,
                (long) page * size < snapshot.totalCount(),
                hasNextBlock ? blockEndPage : actualLastPage,
                hasNextBlock
        );
    }
}
