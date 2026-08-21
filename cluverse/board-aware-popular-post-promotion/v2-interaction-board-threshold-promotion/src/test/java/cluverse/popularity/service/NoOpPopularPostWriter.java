package cluverse.popularity.service;

import cluverse.popularity.domain.PopularPostPromotion;
import cluverse.popularity.domain.PopularityFinalizationTarget;
import cluverse.popularity.service.implement.PopularPostWriter;

import java.time.LocalDateTime;
import java.util.List;

final class NoOpPopularPostWriter implements PopularPostWriter {
    public void promote(PopularPostPromotion promotion) { }
    public List<PopularityFinalizationTarget> findDue(LocalDateTime now, int limit) { return List.of(); }
    public boolean finalizeIfPending(long id, long score, long likes, long comments, LocalDateTime at) {
        return false;
    }
}
