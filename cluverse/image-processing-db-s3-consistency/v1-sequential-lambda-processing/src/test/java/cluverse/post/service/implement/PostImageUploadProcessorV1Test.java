package cluverse.post.service.implement;

import cluverse.post.client.PostImageObjectStorageClient;
import cluverse.post.client.PostImageProcessorClient;
import cluverse.post.domain.PostImageProcessCommand;
import cluverse.post.domain.ProcessedPostImage;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.assertThat;

class PostImageUploadProcessorV1Test {

    @Test
    void 한_이미지가_끝난_뒤_다음_이미지를_처리한다() {
        List<String> calls = new ArrayList<>();
        var storage = storage(calls);
        PostImageProcessorClient client = command -> {
            calls.add("process-" + command.displayOrder());
            return result(command);
        };
        var processor = new PostImageUploadProcessorV1(storage, client, new Semaphore(2));

        processor.process(List.of(image(0), image(1)));

        assertThat(calls).containsExactly("upload-0", "process-0", "upload-1", "process-1");
    }

    private PostImageObjectStorageClient storage(List<String> calls) {
        return new PostImageObjectStorageClient() {
            public void upload(String key, String type, Path source) { calls.add("upload-" + source); }
            public long size(String key) { return 1; }
            public void deleteAll(Collection<String> keys) { }
        };
    }

    private PostImageUploadProcessor.PreparedPostImage image(int order) {
        String root = String.valueOf(order);
        var command = new PostImageProcessCommand(
                UUID.randomUUID(), order, "staging/" + root, "content/" + root, "thumb/" + root, "p1");
        return new PostImageUploadProcessor.PreparedPostImage(Path.of(root), "image/jpeg", 100, command);
    }

    private ProcessedPostImage result(PostImageProcessCommand command) {
        return new ProcessedPostImage(command.displayOrder(),
                new ProcessedPostImage.Metadata(command.contentKey(), "image/jpeg", 1280, 720, 50),
                new ProcessedPostImage.Metadata(command.thumbnailKey(), "image/jpeg", 320, 180, 10));
    }
}
