package cluverse.post.service.implement;

import cluverse.post.domain.Post;
import cluverse.post.service.request.PostCreateRequest;

public interface PostWriter {

    Post create(Long memberId, PostCreateRequest request);
}
