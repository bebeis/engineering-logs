package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularitySnapshot;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class PopularityBatchProcessor {

    private final PopularitySnapshotReader snapshotReader;
    private final PopularityPromotionProcessor promotionProcessor;
    private final PopularityProperties properties;
    private final Clock clock;

    public PopularityBatchProcessor(
            PopularitySnapshotReader snapshotReader,
            PopularityPromotionProcessor promotionProcessor,
            PopularityProperties properties,
            Clock clock
    ) {
        this.snapshotReader = snapshotReader;
        this.promotionProcessor = promotionProcessor;
        this.properties = properties;
        this.clock = clock;
    }

    public synchronized int runDaily() {
        LocalDateTime createdFrom = LocalDateTime.ofInstant(clock.instant(), clock.getZone())
                .minus(properties.promotionWindow());
        LocalDateTime lastCreatedAt = createdFrom;
        long lastPostId = 0L;
        int examined = 0;

        while (true) {
            List<PopularitySnapshot> chunk = snapshotReader.readRecentAfter(
                    createdFrom, lastCreatedAt, lastPostId, properties.scanChunkSize());
            if (chunk.isEmpty()) {
                return examined;
            }
            chunk.forEach(promotionProcessor::evaluate);
            examined += chunk.size();

            PopularitySnapshot last = chunk.getLast();
            lastCreatedAt = last.createdAt();
            lastPostId = last.postId();
            if (chunk.size() < properties.scanChunkSize()) {
                return examined;
            }
        }
    }
}
