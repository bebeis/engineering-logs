package cluverse.post.service;

import cluverse.post.domain.PostListSnapshot;
import cluverse.post.domain.PostSummary;
import cluverse.post.service.implement.PostListReader;
import cluverse.post.service.request.PostListRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostListQueryServiceTest {

    @Test
    void 전체_개수로_현재_페이지_블록과_다음_블록을_계산한다() {
        PostListReader reader = mock(PostListReader.class);
        PostListQueryService service = new PostListQueryService(reader);
        PostListRequest request = new PostListRequest(10L, 1, 20);
        when(reader.read(request)).thenReturn(new PostListSnapshot(List.of(post(250L)), 250L));

        var response = service.getPosts(request);

        assertThat(response.lastPage()).isEqualTo(10);
        assertThat(response.hasNext()).isTrue();
        assertThat(response.hasNextBlock()).isTrue();
    }

    @Test
    void 마지막_블록에서는_실제_마지막_페이지를_반환한다() {
        PostListReader reader = mock(PostListReader.class);
        PostListQueryService service = new PostListQueryService(reader);
        PostListRequest request = new PostListRequest(10L, 2, 20);
        when(reader.read(request)).thenReturn(new PostListSnapshot(List.of(post(1L)), 37L));

        var response = service.getPosts(request);

        assertThat(response.lastPage()).isEqualTo(2);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasNextBlock()).isFalse();
    }

    private PostSummary post(Long postId) {
        return new PostSummary(
                postId,
                "제목",
                "내용 미리보기",
                null,
                10L,
                3L,
                2L,
                "작성자",
                LocalDateTime.of(2026, 1, 20, 12, 0)
        );
    }
}
