package cluverse.reaction.service;

import cluverse.popularity.domain.PopularityTrigger;
import cluverse.popularity.service.PopularityPromotionService;
import cluverse.reaction.service.implement.PostReactionProcessor;

public class PostReactionService {

    private final PostReactionProcessor reactionProcessor;
    private final PopularityPromotionService popularityPromotionService;

    public PostReactionService(
            PostReactionProcessor reactionProcessor,
            PopularityPromotionService popularityPromotionService
    ) {
        this.reactionProcessor = reactionProcessor;
        this.popularityPromotionService = popularityPromotionService;
    }

    public void likePost(long memberId, long postId) {
        reactionProcessor.likePost(memberId, postId);
        popularityPromotionService.tryPromote(postId, PopularityTrigger.LIKE);
    }
}
