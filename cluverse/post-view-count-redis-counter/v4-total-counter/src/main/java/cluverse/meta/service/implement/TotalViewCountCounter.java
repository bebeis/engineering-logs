package cluverse.meta.service.implement;

import cluverse.meta.repository.TotalViewCountRepository;
import cluverse.meta.repository.dto.TotalViewCountResult;
import cluverse.meta.repository.dto.TotalViewCountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TotalViewCountCounter {

    private static final int MAX_REINITIALIZE_COUNT = 3;

    private final TotalViewCountRepository repository;
    private final ViewCountInitializer initializer;
    private final LocalViewCountFallback fallback;

    public ViewCountResult count(Long postId, String cookieId) {
        try {
            for (int attempt = 0; attempt < MAX_REINITIALIZE_COUNT; attempt++) {
                TotalViewCountResult result = repository.count(postId, cookieId);
                if (result.status() == TotalViewCountStatus.REINITIALIZE) {
                    initializer.ensureInitialized(postId);
                    continue;
                }
                return new ViewCountResult(
                        result.viewCount(),
                        result.status() == TotalViewCountStatus.COUNTED,
                        ViewCountSource.REDIS_TOTAL
                );
            }
            throw new IllegalStateException("조회수 카운터 재초기화가 반복됐습니다: " + postId);
        } catch (RedisConnectionFailureException | RedisSystemException exception) {
            return fallback.count(postId, cookieId);
        }
    }
}
