package cluverse.meta.service.implement;

import cluverse.meta.domain.PostViewCountOptimistic;
import cluverse.meta.repository.PostViewCountOptimisticRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class PostMetaWriter {

    private static final int MAX_RETRY_COUNT = 10;
    private static final long RETRY_DELAY_MILLIS = 10L;

    private final PostViewCountOptimisticRepository repository;
    private final TransactionTemplate requiresNewTransactionTemplate;

    public PostMetaWriter(
            PostViewCountOptimisticRepository repository,
            PlatformTransactionManager transactionManager
    ) {
        this.repository = repository;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(
                TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void increaseViewCountOptimistic(Long postId) {
        for (int attempt = 0; attempt < MAX_RETRY_COUNT; attempt++) {
            try {
                requiresNewTransactionTemplate.executeWithoutResult(
                        status -> increaseViewCount(postId));
                return;
            } catch (ObjectOptimisticLockingFailureException
                     | OptimisticLockException
                     | DataIntegrityViolationException exception) {
                if (attempt == MAX_RETRY_COUNT - 1) {
                    throw new IllegalStateException("조회수 증가 재시도를 소진했습니다.", exception);
                }
                pauseBeforeRetry();
            }
        }
    }

    private void increaseViewCount(Long postId) {
        PostViewCountOptimistic viewCount = repository.findById(postId)
                .orElseGet(() -> repository.save(PostViewCountOptimistic.create(postId)));
        viewCount.increase();
        repository.flush();
    }

    private void pauseBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("조회수 증가 재시도가 중단됐습니다.", exception);
        }
    }
}
