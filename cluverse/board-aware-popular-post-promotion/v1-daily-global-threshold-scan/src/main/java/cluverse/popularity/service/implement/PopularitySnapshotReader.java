package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularitySnapshot;

import java.time.LocalDateTime;
import java.util.List;

public interface PopularitySnapshotReader {

    List<PopularitySnapshot> readRecentAfter(
            LocalDateTime createdFrom,
            LocalDateTime lastCreatedAt,
            long lastPostId,
            int limit
    );
}
