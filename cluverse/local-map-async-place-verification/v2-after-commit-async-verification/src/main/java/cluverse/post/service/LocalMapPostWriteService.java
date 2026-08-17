package cluverse.post.service;

import cluverse.post.service.implement.LocalMapPostWriteProcessor;
import cluverse.post.service.implement.PostAccessReader;
import cluverse.post.service.request.PostCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocalMapPostWriteService {

    private final PostAccessReader postAccessReader;
    private final LocalMapPostWriteProcessor processor;

    public Long create(Long memberId, PostCreateRequest request) {
        return postAccessReader.findIdByRequestId(memberId, request.requestId())
                .orElseGet(() -> createOnce(memberId, request));
    }

    private Long createOnce(Long memberId, PostCreateRequest request) {
        try {
            return processor.create(memberId, request);
        } catch (DataIntegrityViolationException exception) {
            return postAccessReader.findIdByRequestId(memberId, request.requestId())
                    .orElseThrow(() -> exception);
        }
    }
}
