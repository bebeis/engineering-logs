package cluverse.meta.service.implement;

import cluverse.meta.repository.TotalViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class LocalViewCountRecovery {

    private final LocalViewCountFallback fallback;
    private final ViewCountInitializer initializer;
    private final TotalViewCountRepository repository;
    private final PostMetaReader postMetaReader;

    public long recover() {
        long recovered = 0;
        for (Map.Entry<Long, AtomicLong> entry : fallback.deltas().entrySet()) {
            long delta = entry.getValue().getAndSet(0);
            if (delta == 0) {
                continue;
            }
            try {
                initializer.ensureInitialized(entry.getKey());
                repository.ensureAtLeast(entry.getKey(), postMetaReader.readViewCount(entry.getKey()));
                repository.increaseBy(entry.getKey(), delta);
                fallback.reflected(entry.getKey());
                recovered += delta;
            } catch (RuntimeException exception) {
                entry.getValue().addAndGet(delta);
                throw exception;
            }
        }
        return recovered;
    }
}
