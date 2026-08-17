package cluverse.post.service;

import cluverse.meta.service.implement.PostMetaReader;
import cluverse.meta.service.implement.PostMetaWriter;
import cluverse.meta.service.implement.ViewCountResult;
import cluverse.meta.service.implement.ViewCountSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostViewCountCommandServiceTest {

    @Test
    void MySQL에서_조회수를_증가시키고_현재값을_반환한다() {
        PostMetaWriter writer = mock(PostMetaWriter.class);
        PostMetaReader reader = mock(PostMetaReader.class);
        PostViewCountCommandService service = new PostViewCountCommandService(writer, reader);
        when(reader.readViewCount(10L)).thenReturn(101L);

        ViewCountResult result = service.increaseViewCount(10L);

        verify(writer).increaseViewCount(10L);
        assertThat(result).isEqualTo(new ViewCountResult(101L, true, ViewCountSource.MYSQL));
    }
}
