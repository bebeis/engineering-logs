package cluverse.meta.repository;

import cluverse.meta.repository.dto.ViewCountDelta;

import java.util.List;

public interface PostViewCountRepositoryCustom {

    void increaseByDeltas(List<ViewCountDelta> deltas);
}
