package cluverse.popularity.service.implement;

import java.time.Clock;
import java.time.LocalDateTime;

public class PopularityFinalizationProcessor {

    private final PopularPostWriter popularPostWriter;
    private final PopularitySnapshotReader snapshotReader;
    private final PopularityProperties properties;
    private final Clock clock;

    public PopularityFinalizationProcessor(
            PopularPostWriter popularPostWriter,
            PopularitySnapshotReader snapshotReader,
            PopularityProperties properties,
            Clock clock
    ) {
        this.popularPostWriter = popularPostWriter;
        this.snapshotReader = snapshotReader;
        this.properties = properties;
        this.clock = clock;
    }

    public int finalizeDue() {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        int finalized = 0;
        for (var target : popularPostWriter.findDue(now, properties.finalizationBatchSize())) {
            var snapshot = snapshotReader.read(target.postId());
            if (snapshot.isEmpty()) {
                continue;
            }
            long score = new PopularityScore(properties.likeWeight(), properties.commentWeight())
                    .calculate(snapshot.get().likeCount(), snapshot.get().commentCount());
            if (popularPostWriter.finalizeIfPending(
                    target.popularPostId(),
                    score,
                    snapshot.get().likeCount(),
                    snapshot.get().commentCount(),
                    now
            )) {
                finalized++;
            }
        }
        return finalized;
    }
}
