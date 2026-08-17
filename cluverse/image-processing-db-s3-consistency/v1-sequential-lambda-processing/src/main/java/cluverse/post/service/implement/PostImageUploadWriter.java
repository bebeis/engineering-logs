package cluverse.post.service.implement;

import cluverse.post.domain.PostImageProcessCommand;
import cluverse.post.domain.ProcessedPostImage;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PostImageUploadWriter {

    Reservation reserve(UUID requestId, String version, List<PostImageProcessCommand> plans);

    Upload complete(long uploadId, List<ProcessedPostImage> results);

    boolean claimForCompensation(long uploadId);

    void completeCompensation(long uploadId, String reason);

    List<Upload> findPendingBefore(Instant threshold);

    List<Upload> findCompletedWithStaging();

    void markStagingCleaned(long uploadId);

    enum Status {
        PENDING, COMPENSATING, COMPLETED, FAILED
    }

    record Reservation(Upload upload, boolean created) {
    }

    record Upload(
            long id,
            UUID requestId,
            String version,
            Status status,
            List<String> stagingKeys,
            List<String> outputKeys
    ) {
        public List<String> allObjectKeys() {
            return java.util.stream.Stream.concat(stagingKeys.stream(), outputKeys.stream()).toList();
        }
    }
}
