package cluverse.meta.service.implement;

import cluverse.meta.repository.TotalViewCountRepository;
import cluverse.meta.repository.dto.ResidentViewCount;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InactiveCounterEvictorTest {

    @Test
    void 최종_체크포인트_뒤_값과_시각이_같을_때만_제거한다() {
        TotalViewCountRepository repository = mock(TotalViewCountRepository.class);
        PostMetaWriter writer = mock(PostMetaWriter.class);
        InactiveCounterEvictor evictor = new InactiveCounterEvictor(repository, writer);
        ResidentViewCount inactive = new ResidentViewCount(10L, 1_000L, 100L);
        when(repository.findInactive()).thenReturn(List.of(inactive));

        evictor.evict();

        verify(writer).checkpointViewCounts(List.of(inactive.toSnapshot()));
        verify(repository).deleteIfUnchanged(inactive);
    }
}
