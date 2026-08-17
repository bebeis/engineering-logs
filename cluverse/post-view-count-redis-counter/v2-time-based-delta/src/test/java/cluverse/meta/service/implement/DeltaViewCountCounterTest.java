package cluverse.meta.service.implement;

import cluverse.meta.repository.DeltaViewCountRepository;
import cluverse.meta.repository.dto.DeltaViewCountResult;
import cluverse.meta.repository.dto.ViewCountDelta;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeltaViewCountCounterTest {

    @Test
    void MySQL_조회수와_Redis_delta를_합산한다() {
        Fixture fixture = new Fixture();
        when(fixture.deltaRepository.count(10L, "viewer-1"))
                .thenReturn(new DeltaViewCountResult(true, 25L));
        when(fixture.reader.readViewCount(10L)).thenReturn(1_000L);

        ViewCountResult result = fixture.counter.count(10L, "viewer-1");

        assertThat(result).isEqualTo(new ViewCountResult(1_025L, true, ViewCountSource.REDIS_DELTA));
    }

    @Test
    void flush가_실패하면_꺼낸_delta를_Redis에_복원한다() {
        Fixture fixture = new Fixture();
        when(fixture.deltaRepository.findPostIds()).thenReturn(List.of(10L));
        when(fixture.deltaRepository.take(10L)).thenReturn(50L);
        doThrow(new IllegalStateException("db unavailable"))
                .when(fixture.writer).applyViewCountDeltas(List.of(new ViewCountDelta(10L, 50L)));

        assertThatThrownBy(fixture.counter::flushTimeBased)
                .isInstanceOf(IllegalStateException.class);
        verify(fixture.deltaRepository).restore(10L, 50L);
    }

    private static class Fixture {
        private final DeltaViewCountRepository deltaRepository = mock(DeltaViewCountRepository.class);
        private final PostMetaReader reader = mock(PostMetaReader.class);
        private final PostMetaWriter writer = mock(PostMetaWriter.class);
        private final DeltaViewCountCounter counter = new DeltaViewCountCounter(
                deltaRepository, reader, writer);
    }
}
