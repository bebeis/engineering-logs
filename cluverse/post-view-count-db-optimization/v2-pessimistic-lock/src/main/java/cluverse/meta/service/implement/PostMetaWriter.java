package cluverse.meta.service.implement;

import cluverse.meta.domain.PostViewCount;
import cluverse.meta.repository.PostViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class PostMetaWriter {

    private final PostViewCountRepository repository;

    public void increaseViewCountPessimistic(Long postId) {
        PostViewCount viewCount = repository.findByPostIdForUpdate(postId)
                .orElseThrow(() -> new IllegalArgumentException("조회수 레코드가 없습니다: " + postId));
        viewCount.increase();
    }
}
