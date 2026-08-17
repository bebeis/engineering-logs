package cluverse.meta.service.implement;

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

    public ViewCountResult count(Long postId, String cookieId) {
        DeltaViewCountResult result = deltaRepository.count(postId, cookieId);
        long viewCount = postMetaReader.readViewCount(postId) + result.delta();
        return new ViewCountResult(viewCount, result.counted(), ViewCountSource.REDIS_DELTA);
    }

    public int flushTimeBased() {
        int flushed = 0;
        for (Long postId : deltaRepository.findPostIds()) {
            if (flush(postId)) {
                flushed++;
            }
        }
        return flushed;
    }

    private boolean flush(Long postId) {
        long delta = deltaRepository.take(postId);
        if (delta == 0) {
            return false;
        }
        try {
            postMetaWriter.applyViewCountDeltas(List.of(new ViewCountDelta(postId, delta)));
            return true;
        } catch (RuntimeException exception) {
            deltaRepository.restore(postId, delta);
            throw exception;
        }
    }
}
