package cluverse.post.service.implement;

import cluverse.post.domain.PostImageProcessCommand;
import cluverse.post.domain.ProcessedPostImage;

import java.nio.file.Path;
import java.util.List;

public interface PostImageUploadProcessor {

    String version();

    List<ProcessedPostImage> process(List<PreparedPostImage> images);

    record PreparedPostImage(
            Path path,
            String contentType,
            long sourceBytes,
            PostImageProcessCommand command
    ) {
    }
}
