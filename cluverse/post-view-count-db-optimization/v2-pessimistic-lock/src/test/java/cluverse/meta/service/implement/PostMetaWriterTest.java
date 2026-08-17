package cluverse.meta.service.implement;

import cluverse.meta.domain.PostViewCount;
import cluverse.meta.repository.PostViewCountRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostMetaWriterTest {

    @Test
    void 비관적_락으로_읽은_조회수를_한_번_증가시킨다() {
        PostViewCountRepository repository = mock(PostViewCountRepository.class);
        PostMetaWriter writer = new PostMetaWriter(repository);
        PostViewCount viewCount = PostViewCount.of(10L, 7);
        when(repository.findByPostIdForUpdate(10L)).thenReturn(Optional.of(viewCount));

        writer.increaseViewCountPessimistic(10L);

        assertThat(viewCount.getViewCount()).isEqualTo(8);
    }

    @Test
    void 조회수_레코드가_없으면_실패한다() {
        PostViewCountRepository repository = mock(PostViewCountRepository.class);
        PostMetaWriter writer = new PostMetaWriter(repository);
        when(repository.findByPostIdForUpdate(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> writer.increaseViewCountPessimistic(10L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
