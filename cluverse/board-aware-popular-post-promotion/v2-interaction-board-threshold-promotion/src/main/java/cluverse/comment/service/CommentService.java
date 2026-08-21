package cluverse.comment.service;

import cluverse.comment.service.implement.CommentProcessor;
import cluverse.popularity.domain.PopularityTrigger;
import cluverse.popularity.service.PopularityPromotionService;

public class CommentService {

    private final CommentProcessor commentProcessor;
    private final PopularityPromotionService popularityPromotionService;

    public CommentService(
            CommentProcessor commentProcessor,
            PopularityPromotionService popularityPromotionService
    ) {
        this.commentProcessor = commentProcessor;
        this.popularityPromotionService = popularityPromotionService;
    }

    public long create(long memberId, long postId, String content) {
        long commentId = commentProcessor.create(memberId, postId, content);
        popularityPromotionService.tryPromote(postId, PopularityTrigger.COMMENT);
        return commentId;
    }
}
