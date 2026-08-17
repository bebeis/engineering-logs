package cluverse.post.client;

import cluverse.post.domain.PostImageProcessCommand;
import cluverse.post.domain.ProcessedPostImage;

public interface PostImageProcessorClient {
    ProcessedPostImage process(PostImageProcessCommand command);

    final class Timeout extends RuntimeException {
        public Timeout(String message, Throwable cause) { super(message, cause); }
    }
}
