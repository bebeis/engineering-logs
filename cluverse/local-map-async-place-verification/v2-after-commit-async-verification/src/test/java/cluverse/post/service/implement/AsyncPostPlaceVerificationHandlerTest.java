package cluverse.post.service.implement;

import cluverse.place.domain.PlaceCandidate;
import cluverse.place.domain.SelectedPlace;
import cluverse.place.service.implement.PlaceSelectionResolver;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsyncPostPlaceVerificationHandlerTest {

    @Test
    void 외부_검증에_성공하면_별도_완료_트랜잭션에_전달한다() {
        PlaceSelectionResolver resolver = mock(PlaceSelectionResolver.class);
        PostPlaceCompletionProcessor completion = mock(PostPlaceCompletionProcessor.class);
        AsyncPostPlaceVerificationHandler handler = new AsyncPostPlaceVerificationHandler(
                resolver, completion);
        SelectedPlace pending = new SelectedPlace(new PlaceCandidate("fp", "카페"), true);
        SelectedPlace verified = new SelectedPlace(new PlaceCandidate("fp", "카페"), true);
        when(resolver.resolve(anyList())).thenReturn(List.of(verified));

        handler.verify(new PostPlaceVerificationRequested(1L, 10L, List.of(pending)));

        verify(completion).complete(10L, List.of(verified));
    }

    @Test
    void 외부_검증이_실패하면_장소_연결을_실행하지_않는다() {
        PlaceSelectionResolver resolver = mock(PlaceSelectionResolver.class);
        PostPlaceCompletionProcessor completion = mock(PostPlaceCompletionProcessor.class);
        AsyncPostPlaceVerificationHandler handler = new AsyncPostPlaceVerificationHandler(
                resolver, completion);
        SelectedPlace pending = new SelectedPlace(new PlaceCandidate("fp", "카페"), true);
        when(resolver.resolve(anyList())).thenThrow(new IllegalStateException("provider timeout"));

        handler.verify(new PostPlaceVerificationRequested(1L, 10L, List.of(pending)));

        verify(completion, never()).complete(org.mockito.ArgumentMatchers.anyLong(), anyList());
    }
}
