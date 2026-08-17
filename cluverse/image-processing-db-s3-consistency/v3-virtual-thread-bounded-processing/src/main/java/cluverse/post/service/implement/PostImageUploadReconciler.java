package cluverse.post.service.implement;

import cluverse.post.client.PostImageObjectStorageClient;

import java.time.Clock;
import java.time.Duration;

public class PostImageUploadReconciler {
    private final PostImageUploadWriter writer;
    private final PostImageObjectStorageClient storage;
    private final Clock clock;
    private final Duration maximumProcessingTime;

    public PostImageUploadReconciler(
            PostImageUploadWriter writer,
            PostImageObjectStorageClient storage,
            Clock clock,
            Duration maximumProcessingTime
    ) {
        this.writer = writer;
        this.storage = storage;
        this.clock = clock;
        this.maximumProcessingTime = maximumProcessingTime;
    }

    public void reconcile() {
        var threshold = clock.instant().minus(maximumProcessingTime);
        for (var upload : writer.findPendingBefore(threshold)) {
            if (writer.claimForCompensation(upload.id())) {
                storage.deleteAll(upload.allObjectKeys());
                writer.completeCompensation(upload.id(), "최대 이미지 처리 시간을 초과했습니다.");
            }
        }
        for (var upload : writer.findCompletedWithStaging()) {
            storage.deleteAll(upload.stagingKeys());
            writer.markStagingCleaned(upload.id());
        }
    }
}
