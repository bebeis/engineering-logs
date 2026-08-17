package cluverse.post.service;

import cluverse.post.service.implement.LocalMapPostWriteProcessor;
import cluverse.post.service.request.PostCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalMapPostWriteService {

    private final LocalMapPostWriteProcessor processor;

    public Long create(Long memberId, PostCreateRequest request) {
        return processor.create(memberId, request);
    }
}
