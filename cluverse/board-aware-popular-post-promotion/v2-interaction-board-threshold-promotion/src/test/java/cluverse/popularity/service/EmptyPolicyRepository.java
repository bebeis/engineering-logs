package cluverse.popularity.service;

import cluverse.popularity.domain.StoredPopularityPolicy;
import cluverse.popularity.repository.BoardPopularityPolicyRepository;

import java.util.Optional;

final class EmptyPolicyRepository implements BoardPopularityPolicyRepository {
    public Optional<StoredPopularityPolicy> findByBoardId(long boardId) { return Optional.empty(); }
    public void save(StoredPopularityPolicy policy) { }
}
