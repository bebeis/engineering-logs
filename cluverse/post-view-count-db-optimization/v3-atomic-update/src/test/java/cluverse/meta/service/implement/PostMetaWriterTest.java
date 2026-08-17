package cluverse.meta.service.implement;

import cluverse.meta.repository.PostViewCountRepository;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PostMetaWriterTest {

    @Test
    void 조회수를_원자적_UPDATE로_증가시킨다() {
        PostViewCountRepository repository = mock(PostViewCountRepository.class);
        PostMetaWriter writer = new PostMetaWriter(repository);

        writer.increaseViewCount(10L);

        verify(repository).increaseCount(10L);
    }
}
