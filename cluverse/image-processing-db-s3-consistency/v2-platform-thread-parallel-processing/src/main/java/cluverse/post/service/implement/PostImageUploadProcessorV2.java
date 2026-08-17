package cluverse.post.service.implement;

import cluverse.post.client.PostImageObjectStorageClient;
import cluverse.post.client.PostImageProcessorClient;
import cluverse.post.domain.ProcessedPostImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class PostImageUploadProcessorV2 implements PostImageUploadProcessor {
    private final PostImageObjectStorageClient storage;
    private final PostImageProcessorClient client;
    private final ExecutorService platformExecutor;

    public PostImageUploadProcessorV2(
            PostImageObjectStorageClient storage,
            PostImageProcessorClient client,
            ExecutorService platformExecutor
    ) {
        this.storage = storage;
        this.client = client;
        this.platformExecutor = platformExecutor;
    }

    @Override
    public String version() { return "v2"; }

    @Override
    public List<ProcessedPostImage> process(List<PreparedPostImage> images) {
        List<CompletableFuture<ProcessedPostImage>> futures = new ArrayList<>();
        RuntimeException submissionFailure = null;
        for (PreparedPostImage image : images) {
            try {
                futures.add(CompletableFuture.supplyAsync(() -> processOne(image), platformExecutor));
            } catch (RuntimeException failure) {
                submissionFailure = failure;
                break;
            }
        }
        try {
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        } catch (RuntimeException taskFailure) {
            if (submissionFailure == null) {
                throw unwrap(taskFailure);
            }
        }
        if (submissionFailure != null) {
            throw submissionFailure;
        }
        return futures.stream().map(CompletableFuture::join).toList();
    }

    private ProcessedPostImage processOne(PreparedPostImage image) {
        storage.upload(image.command().stagingKey(), image.contentType(), image.path());
        ProcessedPostImage result = client.process(image.command());
        verify(result.content());
        verify(result.thumbnail());
        return result;
    }

    private void verify(ProcessedPostImage.Metadata metadata) {
        if (metadata == null || storage.size(metadata.objectKey()) <= 0) {
            throw new IllegalStateException("이미지 처리 결과 객체가 없습니다.");
        }
    }

    private RuntimeException unwrap(RuntimeException failure) {
        return failure.getCause() instanceof RuntimeException cause ? cause : failure;
    }
}
