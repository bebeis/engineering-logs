package cluverse.post.service;

import cluverse.meta.service.implement.PostMetaReader;
import cluverse.meta.service.implement.PostMetaWriter;
import cluverse.meta.service.implement.ViewCountResult;
import cluverse.meta.service.implement.ViewCountSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostViewCountCommandService {

    private final PostMetaWriter postMetaWriter;
    private final PostMetaReader postMetaReader;

    public ViewCountResult increaseViewCount(Long postId) {
        postMetaWriter.increaseViewCount(postId);
        return new ViewCountResult(postMetaReader.readViewCount(postId), true, ViewCountSource.MYSQL);
    }
}
