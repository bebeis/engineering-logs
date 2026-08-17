package cluverse.post.service;

import cluverse.post.service.implement.LocalMapPostWriteProcessor;
import cluverse.post.service.implement.PostAccessReader;
import cluverse.post.service.request.PostCreateRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalMapPostWriteServiceTest {

    @Test
    void 같은_requestId가_처리됐으면_기존_게시글을_반환한다() {
        PostAccessReader reader = mock(PostAccessReader.class);
        LocalMapPostWriteProcessor processor = mock(LocalMapPostWriteProcessor.class);
        LocalMapPostWriteService service = new LocalMapPostWriteService(reader, processor);
        PostCreateRequest request = new PostCreateRequest(
                "request-id", "제목", "본문", List.of());
        when(reader.findIdByRequestId(1L, "request-id")).thenReturn(Optional.of(42L));

        Long result = service.create(1L, request);

        assertThat(result).isEqualTo(42L);
        verify(processor, never()).create(1L, request);
    }
}
