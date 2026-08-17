package cluverse.meta.service.implement;

import cluverse.meta.properties.ViewCountProperties;
import cluverse.meta.repository.TotalViewCountRepository;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewCountInitializerTest {

    @Test
    void 이미_전체_카운터가_있으면_초기화_락을_건드리지_않는다() {
        TotalViewCountRepository repository = mock(TotalViewCountRepository.class);
        PostMetaReader reader = mock(PostMetaReader.class);
        ViewCountInitializer initializer = new ViewCountInitializer(repository, reader, properties());
        when(repository.read(10L)).thenReturn(100L);

        long result = initializer.ensureInitialized(10L);

        assertThat(result).isEqualTo(100L);
        verify(repository, never()).tryAcquireInitialization(eq(10L), anyString());
    }

    private ViewCountProperties properties() {
        return new ViewCountProperties(
                Duration.ofMinutes(30), Duration.ofMinutes(30), 1000, 1000,
                Duration.ofSeconds(1), Duration.ZERO, 3);
    }
}
