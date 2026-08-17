package cluverse.meta.service.implement;

import cluverse.meta.domain.PostViewCountOptimistic;
import cluverse.meta.repository.PostViewCountOptimisticRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PostMetaWriter.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PostMetaWriterTest {

    @Autowired
    private PostMetaWriter postMetaWriter;

    @Autowired
    private PostViewCountOptimisticRepository repository;

    @Test
    void 조회수_레코드가_없으면_생성한_뒤_증가시킨다() {
        postMetaWriter.increaseViewCountOptimistic(20L);

        assertThat(repository.findById(20L))
                .get()
                .extracting(PostViewCountOptimistic::getViewCount)
                .isEqualTo(1);
    }
}
