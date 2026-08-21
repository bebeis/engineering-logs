package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularPostPromotion;
import cluverse.popularity.domain.PopularityPolicy;
import cluverse.popularity.domain.PopularitySnapshot;
import cluverse.popularity.domain.PopularityTrigger;

import java.time.Clock;
import java.time.LocalDateTime;

public class PopularityPromotionProcessor {

    private final PopularitySnapshotReader snapshotReader;
    private final PopularityPolicyStore policyStore;
    private final PopularPostWriter popularPostWriter;
    private final PopularityProperties properties;
    private final Clock clock;

    public PopularityPromotionProcessor(
            PopularitySnapshotReader snapshotReader,
            PopularityPolicyStore policyStore,
            PopularPostWriter popularPostWriter,
            PopularityProperties properties,
            Clock clock
    ) {
        this.snapshotReader = snapshotReader;
        this.policyStore = policyStore;
        this.popularPostWriter = popularPostWriter;
        this.properties = properties;
        this.clock = clock;
    }

    public boolean evaluate(long postId, PopularityTrigger trigger) {
        return snapshotReader.read(postId)
                .map(snapshot -> evaluate(snapshot, trigger))
                .orElse(false);
    }

    private boolean evaluate(PopularitySnapshot snapshot, PopularityTrigger trigger) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        LocalDateTime finalizeAt = snapshot.createdAt().plus(properties.promotionWindow());
        if (snapshot.deleted() || !now.isBefore(finalizeAt)) {
            return false;
        }

        PopularityPolicy policy = policyStore.read(snapshot.boardId());
        long score = score(snapshot.likeCount(), snapshot.commentCount());
        if (score < policy.promotionScore()) {
            return false;
        }

        popularPostWriter.promote(new PopularPostPromotion(
                snapshot.postId(),
                snapshot.boardId(),
                now,
                finalizeAt,
                score,
                snapshot.likeCount(),
                snapshot.commentCount(),
                policy.promotionScore(),
                trigger
        ));
        return true;
    }

    private long score(long likeCount, long commentCount) {
        return new PopularityScore(properties.likeWeight(), properties.commentWeight())
                .calculate(likeCount, commentCount);
    }
}
