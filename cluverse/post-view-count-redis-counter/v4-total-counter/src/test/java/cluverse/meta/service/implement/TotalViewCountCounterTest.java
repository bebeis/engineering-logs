package cluverse.meta.service.implement;

import cluverse.meta.repository.TotalViewCountRepository;
import cluverse.meta.repository.dto.TotalViewCountResult;
import cluverse.meta.repository.dto.TotalViewCountStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TotalViewCountCounterTest {

    @Test
    void 제거_경쟁으로_카운터가_사라지면_재초기화한_뒤_집계한다() {
        TotalViewCountRepository repository = mock(TotalViewCountRepository.class);
        ViewCountInitializer initializer = mock(ViewCountInitializer.class);
        LocalViewCountFallback fallback = mock(LocalViewCountFallback.class);
        TotalViewCountCounter counter = new TotalViewCountCounter(repository, initializer, fallback);
        when(repository.count(10L, "cookie-1"))
                .thenReturn(new TotalViewCountResult(TotalViewCountStatus.REINITIALIZE, 0L))
                .thenReturn(new TotalViewCountResult(TotalViewCountStatus.COUNTED, 101L));

        ViewCountResult result = counter.count(10L, "cookie-1");

        verify(initializer).ensureInitialized(10L);
        assertThat(result.viewCount()).isEqualTo(101L);
    }
}
