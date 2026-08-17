package cluverse.meta.service.implement;

import cluverse.meta.properties.ViewCountProperties;
import cluverse.meta.repository.DeltaViewCountRepository;
import cluverse.meta.repository.dto.DeltaViewCountResult;
import cluverse.meta.repository.dto.ViewCountDelta;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeltaViewCountCounterTest {

    @Test
    void 임계치에_도달하면_delta를_DB에_반영한다() {
        DeltaViewCountRepository deltaRepository = mock(DeltaViewCountRepository.class);
        PostMetaReader reader = mock(PostMetaReader.class);
        PostMetaWriter writer = mock(PostMetaWriter.class);
        DeltaViewCountCounter counter = new DeltaViewCountCounter(
                deltaRepository,
                reader,
                writer,
                new ViewCountProperties(Duration.ofMinutes(30), 100L)
        );
        when(deltaRepository.count(10L, "viewer-1"))
                .thenReturn(new DeltaViewCountResult(true, 100L));
        when(deltaRepository.take(10L)).thenReturn(100L);
        when(reader.readViewCount(10L)).thenReturn(1_100L);

        ViewCountResult result = counter.count(10L, "viewer-1");

        verify(writer).applyViewCountDeltas(List.of(new ViewCountDelta(10L, 100L)));
        assertThat(result.viewCount()).isEqualTo(1_100L);
    }
}
