package cluverse.post.service;

import cluverse.meta.service.implement.TotalViewCountCounter;
import cluverse.meta.service.implement.ViewCountResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostViewCountCommandService {

    private final TotalViewCountCounter counter;

    public ViewCountResult increaseViewCount(Long postId, String cookieId) {
        return counter.count(postId, cookieId);
    }
}
