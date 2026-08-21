package cluverse.popularity.repository;

import cluverse.popularity.domain.StoredPopularityPolicy;

import java.util.Optional;

public interface BoardPopularityPolicyRepository {

    Optional<StoredPopularityPolicy> findByBoardId(long boardId);

    void save(StoredPopularityPolicy policy);
}
