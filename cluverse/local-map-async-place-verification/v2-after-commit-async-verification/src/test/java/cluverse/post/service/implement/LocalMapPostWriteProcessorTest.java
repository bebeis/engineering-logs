package cluverse.post.service.implement;

import cluverse.meta.service.implement.PostMetaWriter;
import cluverse.place.domain.PlaceCandidate;
import cluverse.place.domain.SelectedPlace;
import cluverse.post.domain.Post;
import cluverse.post.service.request.PostCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalMapPostWriteProcessorTest {

    @Test
    void 게시글과_메타만_저장하고_장소_검증은_이벤트로_요청한다() {
        PostWriter postWriter = mock(PostWriter.class);
        PostMetaWriter metaWriter = mock(PostMetaWriter.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        LocalMapPostWriteProcessor processor = new LocalMapPostWriteProcessor(
                postWriter, metaWriter, publisher);
        SelectedPlace place = new SelectedPlace(new PlaceCandidate("fp", "카페"), true);
        PostCreateRequest request = new PostCreateRequest(
                "request-id", "제목", "본문", List.of(place));
        when(postWriter.create(1L, request)).thenReturn(new Post(10L));

        processor.create(1L, request);

        verify(metaWriter).createViewCount(10L);
        verify(publisher).publishEvent(
                new PostPlaceVerificationRequested(1L, 10L, List.of(place)));
    }
}
