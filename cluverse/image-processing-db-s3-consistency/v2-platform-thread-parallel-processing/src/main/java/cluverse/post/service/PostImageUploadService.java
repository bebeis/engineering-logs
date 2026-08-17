package cluverse.post.service;

import cluverse.post.client.PostImageObjectStorageClient;
import cluverse.post.client.PostImageProcessorClient;
import cluverse.post.domain.ProcessedPostImage;
import cluverse.post.service.implement.PostImageUploadProcessor;
import cluverse.post.service.implement.PostImageUploadWriter;

import java.util.List;
import java.util.UUID;

public class PostImageUploadService {
    private final PostImageUploadProcessor processor;
    private final PostImageUploadWriter writer;
    private final PostImageObjectStorageClient storage;

    public PostImageUploadService(
            PostImageUploadProcessor processor,
            PostImageUploadWriter writer,
            PostImageObjectStorageClient storage
    ) {
        this.processor = processor;
        this.writer = writer;
        this.storage = storage;
    }

    public PostImageUploadWriter.Upload upload(
            UUID requestId,
            List<PostImageUploadProcessor.PreparedPostImage> images
    ) {
        var plans = images.stream().map(PostImageUploadProcessor.PreparedPostImage::command).toList();
        var reservation = writer.reserve(requestId, processor.version(), plans);
        if (!reservation.created()) {
            return resolveRetry(reservation.upload());
        }
        try {
            List<ProcessedPostImage> results = processor.process(images);
            PostImageUploadWriter.Upload completed = writer.complete(reservation.upload().id(), results);
            cleanupStaging(completed);
            return completed;
        } catch (PostImageProcessorClient.Timeout timeout) {
            throw timeout;
        } catch (RuntimeException failure) {
            if (writer.claimForCompensation(reservation.upload().id())) {
                storage.deleteAll(reservation.upload().allObjectKeys());
                writer.completeCompensation(reservation.upload().id(), failure.getMessage());
            }
            throw failure;
        }
    }

    private void cleanupStaging(PostImageUploadWriter.Upload completed) {
        try {
            storage.deleteAll(completed.stagingKeys());
            writer.markStagingCleaned(completed.id());
        } catch (RuntimeException ignored) {
            // COMPLETED 결과는 유지하고 조정 작업이 staging 삭제를 재시도한다.
        }
    }

    private PostImageUploadWriter.Upload resolveRetry(PostImageUploadWriter.Upload upload) {
        return switch (upload.status()) {
            case COMPLETED -> upload;
            case PENDING, COMPENSATING -> throw new IllegalStateException("같은 requestId의 업로드가 진행 중입니다.");
            case FAILED -> throw new IllegalStateException("실패한 requestId는 재사용할 수 없습니다.");
        };
    }
}
