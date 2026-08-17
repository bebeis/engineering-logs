package cluverse.post.service;

import cluverse.meta.service.implement.DeltaViewCountCounter;
import cluverse.meta.service.implement.ViewCountResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostViewCountCommandService {

    private final DeltaViewCountCounter counter;

    public ViewCountResult increaseViewCount(Long postId, String cookieId) {
        return counter.count(postId, cookieId);
    }
}
