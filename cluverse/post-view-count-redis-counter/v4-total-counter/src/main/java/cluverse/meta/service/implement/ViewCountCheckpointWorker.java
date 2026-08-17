package cluverse.meta.service.implement;

import cluverse.meta.repository.TotalViewCountRepository;
import cluverse.meta.repository.dto.ResidentViewCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ViewCountCheckpointWorker {

    private final TotalViewCountRepository repository;
    private final PostMetaWriter postMetaWriter;

    public int checkpoint() {
        List<ResidentViewCount> counters = repository.findAll();
        postMetaWriter.checkpointViewCounts(
                counters.stream().map(ResidentViewCount::toSnapshot).toList());
        return counters.size();
    }
}
