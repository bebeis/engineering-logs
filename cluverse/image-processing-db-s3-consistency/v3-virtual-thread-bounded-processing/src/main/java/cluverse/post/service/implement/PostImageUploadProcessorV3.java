package cluverse.post.service.implement;

import cluverse.post.client.PostImageObjectStorageClient;
import cluverse.post.client.PostImageProcessorClient;
import cluverse.post.domain.ProcessedPostImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;

public class PostImageUploadProcessorV3 implements PostImageUploadProcessor {
    private final PostImageObjectStorageClient storage;
    private final PostImageProcessorClient client;
    private final ExecutorService virtualExecutor;
    private final Semaphore remoteCalls;

    public PostImageUploadProcessorV3(
            PostImageObjectStorageClient storage,
            PostImageProcessorClient client,
            ExecutorService virtualExecutor,
            Semaphore remoteCalls
    ) {
        this.storage = storage;
        this.client = client;
        this.virtualExecutor = virtualExecutor;
        this.remoteCalls = remoteCalls;
    }

    @Override
    public String version() { return "v3"; }

    @Override
    public List<ProcessedPostImage> process(List<PreparedPostImage> images) {
        List<CompletableFuture<ProcessedPostImage>> futures = new ArrayList<>();
        RuntimeException submissionFailure = null;
        for (PreparedPostImage image : images) {
            try {
                futures.add(CompletableFuture.supplyAsync(() -> processWithPermit(image), virtualExecutor));
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

    private ProcessedPostImage processWithPermit(PreparedPostImage image) {
        acquire();
        try {
            storage.upload(image.command().stagingKey(), image.contentType(), image.path());
            ProcessedPostImage result = client.process(image.command());
            verify(result.content());
            verify(result.thumbnail());
            return result;
        } finally {
            remoteCalls.release();
        }
    }

    private void acquire() {
        try {
            remoteCalls.acquire();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("이미지 처리 대기가 중단됐습니다.", exception);
        }
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
