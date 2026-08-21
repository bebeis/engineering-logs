package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularityPolicySample;

import java.time.LocalDateTime;
import java.util.List;

public interface PopularityPolicySampleReader {

    List<Long> readBoardIds(LocalDateTime sampleStart, LocalDateTime sampleEnd);

    List<PopularityPolicySample> readSamples(
            long boardId,
            LocalDateTime sampleStart,
            LocalDateTime sampleEnd
    );
}
