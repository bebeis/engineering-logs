package cluverse.meta.service.implement;

import cluverse.meta.repository.TotalViewCountRepository;
import cluverse.meta.repository.dto.ResidentViewCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InactiveCounterEvictor {

    private final TotalViewCountRepository repository;
    private final PostMetaWriter postMetaWriter;

    public int evict() {
        List<ResidentViewCount> counters = repository.findInactive();
        if (counters.isEmpty()) {
            return 0;
        }
        postMetaWriter.checkpointViewCounts(
                counters.stream().map(ResidentViewCount::toSnapshot).toList());
        int evicted = 0;
        for (ResidentViewCount counter : counters) {
            if (repository.deleteIfUnchanged(counter)) {
                evicted++;
            }
        }
        return evicted;
    }
}
