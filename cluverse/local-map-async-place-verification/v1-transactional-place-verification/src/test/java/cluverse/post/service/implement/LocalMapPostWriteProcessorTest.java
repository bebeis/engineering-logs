package cluverse.post.service.implement;

import cluverse.meta.service.implement.PostMetaWriter;
import cluverse.place.domain.Place;
import cluverse.place.domain.PlaceCandidate;
import cluverse.place.domain.SelectedPlace;
import cluverse.place.service.implement.PlaceSelectionResolver;
import cluverse.place.service.implement.PlaceWriter;
import cluverse.post.domain.Post;
import cluverse.post.service.request.PostCreateRequest;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalMapPostWriteProcessorTest {

    @Test
    void 외부_검증과_장소_저장을_게시글_트랜잭션_안에서_수행한다() {
        PlaceSelectionResolver resolver = mock(PlaceSelectionResolver.class);
        PlaceWriter placeWriter = mock(PlaceWriter.class);
        PostWriter postWriter = mock(PostWriter.class);
        PostMetaWriter metaWriter = mock(PostMetaWriter.class);
        LocalMapPostWriteProcessor processor = new LocalMapPostWriteProcessor(
                resolver, placeWriter, postWriter, metaWriter);
        PostCreateRequest request = new PostCreateRequest("제목", "본문", List.of());
        SelectedPlace selected = new SelectedPlace(new PlaceCandidate("fp", "카페"), true);
        Post post = new Post(10L);
        when(resolver.resolve(List.of())).thenReturn(List.of(selected));
        when(placeWriter.upsertAll(List.of(selected.candidate())))
                .thenReturn(List.of(new Place(20L, "fp", "카페")));
        when(postWriter.create(1L, request)).thenReturn(post);

        Long postId = processor.create(1L, request);

        assertThat(postId).isEqualTo(10L);
        InOrder order = inOrder(resolver, placeWriter, postWriter);
        order.verify(resolver).resolve(List.of());
        order.verify(placeWriter).upsertAll(List.of(selected.candidate()));
        order.verify(postWriter).create(1L, request);
        verify(metaWriter).createViewCount(10L);
    }
}
