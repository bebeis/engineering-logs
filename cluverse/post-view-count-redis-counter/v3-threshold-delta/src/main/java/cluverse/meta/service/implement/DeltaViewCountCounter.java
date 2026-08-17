package cluverse.meta.service.implement;

import cluverse.meta.properties.ViewCountProperties;
import cluverse.meta.repository.DeltaViewCountRepository;
import cluverse.meta.repository.dto.DeltaViewCountResult;
import cluverse.meta.repository.dto.ViewCountDelta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeltaViewCountCounter {

    private final DeltaViewCountRepository deltaRepository;
    private final PostMetaReader postMetaReader;
    private final PostMetaWriter postMetaWriter;
    private final ViewCountProperties properties;

    public ViewCountResult count(Long postId, String cookieId) {
        DeltaViewCountResult result = deltaRepository.count(postId, cookieId);
        if (result.delta() >= properties.threshold()) {
            flush(postId);
        }
        long currentDelta = result.delta() >= properties.threshold() ? 0L : result.delta();
        long viewCount = postMetaReader.readViewCount(postId) + currentDelta;
        return new ViewCountResult(viewCount, result.counted(), ViewCountSource.REDIS_DELTA);
    }

    private void flush(Long postId) {
        long delta = deltaRepository.take(postId);
        if (delta == 0) {
            return;
        }
        try {
            postMetaWriter.applyViewCountDeltas(List.of(new ViewCountDelta(postId, delta)));
        } catch (RuntimeException exception) {
            deltaRepository.restore(postId, delta);
            throw exception;
        }
    }
}
