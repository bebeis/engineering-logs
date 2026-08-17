package cluverse.meta.service.implement;

import cluverse.meta.repository.PostViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class PostMetaWriter {

    private final PostViewCountRepository repository;

    public void increaseViewCount(Long postId) {
        repository.increaseCount(postId);
    }
}
