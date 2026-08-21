package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularPostPromotion;
import cluverse.popularity.domain.PopularityFinalizationTarget;

import java.time.LocalDateTime;
import java.util.List;

public interface PopularPostWriter {

    void promote(PopularPostPromotion promotion);

    List<PopularityFinalizationTarget> findDue(LocalDateTime now, int limit);

    boolean finalizeIfPending(
            long popularPostId,
            long score,
            long likeCount,
            long commentCount,
            LocalDateTime finalizedAt
    );
}
