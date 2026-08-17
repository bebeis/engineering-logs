package cluverse.post.service;

import cluverse.meta.service.implement.PostMetaWriter;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PostViewCountCommandServiceTest {

    @Test
    void 조회수_증가를_원자적_UPDATE_방식에_위임한다() {
        PostMetaWriter writer = mock(PostMetaWriter.class);
        PostViewCountCommandService service = new PostViewCountCommandService(writer);

        service.increaseViewCount(10L);

        verify(writer).increaseViewCount(10L);
    }
}
