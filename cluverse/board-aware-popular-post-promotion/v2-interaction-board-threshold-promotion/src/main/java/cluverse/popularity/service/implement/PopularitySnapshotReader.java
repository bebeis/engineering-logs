package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularitySnapshot;

import java.util.Optional;

public interface PopularitySnapshotReader {

    Optional<PopularitySnapshot> read(long postId);
}
