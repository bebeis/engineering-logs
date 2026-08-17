package cluverse.post.service.implement;

import cluverse.post.domain.PostCursorSlice;
import cluverse.post.repository.PostPageQueryRepository;
import cluverse.post.repository.PostSummaryQueryRepository;
import cluverse.post.repository.dto.PostIdSlice;
import cluverse.post.service.request.PostCursorRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostListReader {

    private final PostPageQueryRepository pageRepository;
    private final PostSummaryQueryRepository summaryRepository;

    public PostCursorSlice read(PostCursorRequest request) {
        PostIdSlice slice = pageRepository.findPostIds(request);
        return new PostCursorSlice(
                summaryRepository.findByIds(slice.postIds()),
                slice.hasMore()
        );
    }

    public boolean existsNewerThan(PostCursorRequest request) {
        return pageRepository.existsNewerThan(request.boardId(), request.exclusiveDateEnd());
    }
}
