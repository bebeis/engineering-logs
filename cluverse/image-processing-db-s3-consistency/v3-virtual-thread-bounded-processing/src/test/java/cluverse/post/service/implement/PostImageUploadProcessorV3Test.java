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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class PostImageUploadProcessorV3Test {
    private ExecutorService executor;

    @AfterEach
    void closeExecutor() throws InterruptedException {
        executor.shutdownNow();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    void Virtual_Thread_수와_무관하게_외부_호출_동시성을_제한한다() {
        AtomicInteger inFlight = new AtomicInteger();
        AtomicInteger maxInFlight = new AtomicInteger();
        PostImageProcessorClient client = command -> {
            int current = inFlight.incrementAndGet();
            maxInFlight.accumulateAndGet(current, Math::max);
            try { Thread.sleep(20); return result(command); }
            catch (InterruptedException exception) { throw new IllegalStateException(exception); }
            finally { inFlight.decrementAndGet(); }
        };
        executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
        var processor = new PostImageUploadProcessorV3(storage(), client, executor, new Semaphore(1));

        assertThat(processor.process(List.of(image(0), image(1), image(2)))).hasSize(3);
        assertThat(maxInFlight).hasValue(1);
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
