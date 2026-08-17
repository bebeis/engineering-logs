package cluverse.post.service.implement;

import cluverse.post.domain.Post;

import java.util.Optional;

public interface PostAccessReader {

    Optional<Long> findIdByRequestId(Long memberId, String requestId);

    Post readOrThrow(Long postId);
}
