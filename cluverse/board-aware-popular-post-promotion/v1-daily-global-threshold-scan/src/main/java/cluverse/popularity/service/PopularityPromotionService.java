package cluverse.popularity.service;

import cluverse.popularity.service.implement.PopularityBatchProcessor;

public class PopularityPromotionService {

    private final PopularityBatchProcessor batchProcessor;

    public PopularityPromotionService(PopularityBatchProcessor batchProcessor) {
        this.batchProcessor = batchProcessor;
    }

    public int promoteDaily() {
        return batchProcessor.runDaily();
    }
}
