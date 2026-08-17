package cluverse.post.service;

import cluverse.meta.service.implement.PostMetaWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostViewCountCommandService {

    private final PostMetaWriter postMetaWriter;

    public void increaseViewCount(Long postId) {
        postMetaWriter.increaseViewCountOptimistic(postId);
    }
}
