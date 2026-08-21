package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularityPolicy;
import cluverse.popularity.domain.StoredPopularityPolicy;
import cluverse.popularity.repository.BoardPopularityPolicyRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PopularityPolicyStore {

    private final BoardPopularityPolicyRepository repository;
    private final PopularityProperties properties;
    private final Map<Long, PopularityPolicy> cache = new ConcurrentHashMap<>();

    public PopularityPolicyStore(
            BoardPopularityPolicyRepository repository,
            PopularityProperties properties
    ) {
        this.repository = repository;
        this.properties = properties;
    }

    public PopularityPolicy read(long boardId) {
        return cache.computeIfAbsent(boardId, this::readOrDefault);
    }

    public void replace(
            long boardId,
            PopularityPolicy policy,
            int sampleSize,
            String source,
            LocalDateTime computedAt
    ) {
        repository.save(new StoredPopularityPolicy(boardId, policy, sampleSize, source, computedAt));
        cache.put(boardId, policy);
    }

    private PopularityPolicy readOrDefault(long boardId) {
        return repository.findByBoardId(boardId)
                .map(StoredPopularityPolicy::policy)
                .orElseGet(() -> new PopularityPolicy(properties.defaultPromotionScore()));
    }
}
