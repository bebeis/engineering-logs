package cluverse.meta.service.implement;

import cluverse.meta.repository.PostViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostMetaReader {

    private final PostViewCountRepository repository;

    public long readViewCount(Long postId) {
        return repository.readViewCount(postId);
    }
}
