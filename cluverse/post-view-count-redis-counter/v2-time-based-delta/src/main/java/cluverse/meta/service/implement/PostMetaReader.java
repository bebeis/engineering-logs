package cluverse.meta.service.implement;

import cluverse.meta.repository.PostViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostMetaReader {

    private final PostViewCountRepository repository;

    public long readViewCount(Long postId) {
        return repository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("조회수 레코드가 없습니다: " + postId))
                .getViewCount();
    }
}
