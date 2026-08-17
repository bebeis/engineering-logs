package cluverse.meta.scheduler;

import cluverse.meta.service.implement.DeltaViewCountCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewCountScheduler {

    private final DeltaViewCountCounter counter;

    @Scheduled(fixedDelayString = "${view-count.delta-flush-interval:1m}")
    public void flushTimeBasedDelta() {
        counter.flushTimeBased();
    }
}
