package cluverse.popularity.service;

import cluverse.popularity.domain.PopularityTrigger;
import cluverse.popularity.service.implement.PopularityPromotionProcessor;

import java.util.function.Consumer;

public class PopularityPromotionService {

    private final PopularityPromotionProcessor processor;
    private final Consumer<RuntimeException> failureReporter;

    public PopularityPromotionService(
            PopularityPromotionProcessor processor,
            Consumer<RuntimeException> failureReporter
    ) {
        this.processor = processor;
        this.failureReporter = failureReporter;
    }

    public void tryPromote(long postId, PopularityTrigger trigger) {
        try {
            processor.evaluate(postId, trigger);
        } catch (RuntimeException failure) {
            failureReporter.accept(failure);
        }
    }
}
