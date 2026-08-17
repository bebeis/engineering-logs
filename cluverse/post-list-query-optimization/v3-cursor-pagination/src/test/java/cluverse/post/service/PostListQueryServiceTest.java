package cluverse.post.service;

import cluverse.post.domain.PostCursorSlice;
import cluverse.post.domain.PostSummary;
import cluverse.post.service.implement.PostListReader;
import cluverse.post.service.request.PostCursorDirection;
import cluverse.post.service.request.PostCursorRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostListQueryServiceTest {

    @Test
    void 첫_글과_마지막_글이_이전과_다음_커서가_된다() {
        PostListReader reader = mock(PostListReader.class);
        PostListQueryService service = new PostListQueryService(reader);
        PostCursorRequest request = new PostCursorRequest(3L, 2, null, null, null, null);
        LocalDateTime firstCreatedAt = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime lastCreatedAt = LocalDateTime.of(2026, 1, 19, 9, 0);
        when(reader.read(request)).thenReturn(new PostCursorSlice(
                List.of(post(10L, firstCreatedAt), post(7L, lastCreatedAt)),
                true
        ));

        var response = service.getPosts(request);

        assertThat(response.prevCursor().createdAt()).isEqualTo(firstCreatedAt);
        assertThat(response.prevCursor().postId()).isEqualTo(10L);
        assertThat(response.nextCursor().createdAt()).isEqualTo(lastCreatedAt);
        assertThat(response.nextCursor().postId()).isEqualTo(7L);
    }

    @Test
    void 날짜_진입은_앵커보다_최신_글이_있을_때_hasPrev가_true다() {
        PostListReader reader = mock(PostListReader.class);
        PostListQueryService service = new PostListQueryService(reader);
        PostCursorRequest request = new PostCursorRequest(
                3L, 20, LocalDate.of(2026, 1, 20), null, null, null);
        when(reader.read(request)).thenReturn(new PostCursorSlice(List.of(), false));
        when(reader.existsNewerThan(request)).thenReturn(true);

        var response = service.getPosts(request);

        assertThat(response.hasPrev()).isTrue();
        assertThat(response.hasNext()).isFalse();
    }

    @Test
    void PREV_조회는_더_최신_페이지의_존재를_슬라이스로_판단한다() {
        PostListReader reader = mock(PostListReader.class);
        PostListQueryService service = new PostListQueryService(reader);
        PostCursorRequest request = new PostCursorRequest(
                3L,
                20,
                null,
                LocalDateTime.of(2026, 1, 20, 12, 0),
                100L,
                PostCursorDirection.PREV
        );
        when(reader.read(request)).thenReturn(new PostCursorSlice(List.of(), true));

        var response = service.getPosts(request);

        assertThat(response.hasPrev()).isTrue();
        assertThat(response.hasNext()).isTrue();
    }

    private PostSummary post(Long postId, LocalDateTime createdAt) {
        return new PostSummary(postId, "제목", "미리보기", createdAt);
    }
}
