package cluverse.meta.service.implement;

import cluverse.meta.repository.PostViewCountRepository;
import cluverse.meta.repository.dto.ViewCountSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class PostMetaWriter {

    private final PostViewCountRepository repository;

    public void checkpointViewCounts(List<ViewCountSnapshot> snapshots) {
        repository.checkpointViewCounts(snapshots);
    }
}
