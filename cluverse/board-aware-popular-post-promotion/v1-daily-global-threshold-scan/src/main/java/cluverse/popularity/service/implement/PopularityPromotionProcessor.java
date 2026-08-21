package cluverse.popularity.service.implement;

import cluverse.popularity.domain.PopularPostPromotion;
import cluverse.popularity.domain.PopularitySnapshot;

import java.time.Clock;
import java.time.LocalDateTime;

public class PopularityPromotionProcessor {

    private static final String VERSION = "v1";

    private final PopularPostWriter popularPostWriter;
    private final PopularityProperties properties;
    private final Clock clock;

    public PopularityPromotionProcessor(
            PopularPostWriter popularPostWriter,
            PopularityProperties properties,
            Clock clock
    ) {
        this.popularPostWriter = popularPostWriter;
        this.properties = properties;
        this.clock = clock;
    }

    public boolean evaluate(PopularitySnapshot snapshot) {
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        LocalDateTime finalizeAt = snapshot.createdAt().plus(properties.promotionWindow());
        if (snapshot.deleted() || !now.isBefore(finalizeAt)) {
            return false;
        }

        long score = new PopularityScore(properties.likeWeight(), properties.commentWeight())
                .calculate(snapshot.likeCount(), snapshot.commentCount());
        if (score < properties.defaultPromotionScore()) {
            return false;
        }

        popularPostWriter.promote(new PopularPostPromotion(
                VERSION,
                snapshot.postId(),
                snapshot.boardId(),
                now,
                finalizeAt,
                score,
                properties.defaultPromotionScore()
        ));
        return true;
    }
}
