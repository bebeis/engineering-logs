package cluverse.post.service;

import cluverse.post.domain.PostPageData;
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
        int page = request.pageOrDefault();
        int size = request.sizeOrDefault();
        long searchLimit = pageBlockSearchLimit(page, size);
        PostPageData pageData = postListReader.read(request, searchLimit);
        PageBlock pageBlock = resolvePageBlock(page, size, searchLimit, pageData.cappedCount());

        return PostPageResponse.from(
                pageData.posts(),
                page,
                size,
                pageData.hasNext(),
                pageBlock.lastPage(),
                pageBlock.hasNextBlock()
        );
    }

    private long pageBlockSearchLimit(int page, int size) {
        int blockIndex = (page - 1) / PAGE_BLOCK_SIZE;
        return (long) (blockIndex + 1) * size * PAGE_BLOCK_SIZE + 1;
    }

    private PageBlock resolvePageBlock(int page, int size, long searchLimit, long cappedCount) {
        if (cappedCount >= searchLimit) {
            int blockIndex = (page - 1) / PAGE_BLOCK_SIZE;
            return new PageBlock((blockIndex + 1) * PAGE_BLOCK_SIZE, true);
        }
        int lastPage = (int) Math.max(1, (cappedCount + size - 1) / size);
        return new PageBlock(lastPage, false);
    }

    private record PageBlock(int lastPage, boolean hasNextBlock) {
    }
}
