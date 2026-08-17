package cluverse.post.service.implement;

import cluverse.post.repository.PostListCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostListCacheInvalidator {

    private final PostListCacheRepository cacheRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void invalidate(PostListChangedEvent event) {
        try {
            cacheRepository.invalidate(event.boardId());
        } catch (RuntimeException exception) {
            log.warn("커밋 후 캐시 무효화에 실패했습니다. TTL 만료로 복구합니다. boardId={}",
                    event.boardId(), exception);
        }
    }
}
