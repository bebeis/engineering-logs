package cluverse.post.service.implement;

import cluverse.post.client.PostImageObjectStorageClient;
import cluverse.post.client.PostImageProcessorClient;
import cluverse.post.domain.PostImageProcessCommand;
import cluverse.post.domain.ProcessedPostImage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PostImageUploadProcessorV2Test {
    private ExecutorService executor;

    @AfterEach
    void closeExecutor() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    void 고정_스레드_풀에서_두_외부_호출을_겹쳐_실행한다() {
        CyclicBarrier barrier = new CyclicBarrier(2);
        PostImageProcessorClient client = command -> {
            try { barrier.await(1, TimeUnit.SECONDS); }
            catch (Exception exception) { throw new IllegalStateException(exception); }
            return result(command);
        };
        executor = Executors.newFixedThreadPool(2);
        var processor = new PostImageUploadProcessorV2(storage(), client, executor);

        assertThat(processor.process(List.of(image(0), image(1)))).hasSize(2);
    }

    private PostImageObjectStorageClient storage() {
        return new PostImageObjectStorageClient() {
            public void upload(String key, String type, Path source) { }
            public long size(String key) { return 1; }
            public void deleteAll(Collection<String> keys) { }
        };
    }

    private PostImageUploadProcessor.PreparedPostImage image(int order) {
        var command = new PostImageProcessCommand(UUID.randomUUID(), order,
                "staging/" + order, "content/" + order, "thumb/" + order, "p1");
        return new PostImageUploadProcessor.PreparedPostImage(Path.of(String.valueOf(order)), "image/jpeg", 100, command);
    }

    private ProcessedPostImage result(PostImageProcessCommand command) {
        return new ProcessedPostImage(command.displayOrder(),
                new ProcessedPostImage.Metadata(command.contentKey(), "image/jpeg", 1280, 720, 50),
                new ProcessedPostImage.Metadata(command.thumbnailKey(), "image/jpeg", 320, 180, 10));
    }
}
