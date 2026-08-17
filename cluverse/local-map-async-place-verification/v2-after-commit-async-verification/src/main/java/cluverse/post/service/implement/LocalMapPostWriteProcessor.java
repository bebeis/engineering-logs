package cluverse.post.service.implement;

import cluverse.meta.service.implement.PostMetaWriter;
import cluverse.post.domain.Post;
import cluverse.post.service.request.PostCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LocalMapPostWriteProcessor {

    private final PostWriter postWriter;
    private final PostMetaWriter postMetaWriter;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long create(Long memberId, PostCreateRequest request) {
        Post post = postWriter.create(memberId, request);
        postMetaWriter.createViewCount(post.id());
        if (!request.places().isEmpty()) {
            eventPublisher.publishEvent(
                    new PostPlaceVerificationRequested(memberId, post.id(), request.places()));
        }
        return post.id();
    }
}
