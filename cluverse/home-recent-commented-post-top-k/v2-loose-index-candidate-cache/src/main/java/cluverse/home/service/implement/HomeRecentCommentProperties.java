package cluverse.home.service.implement;

import java.time.Duration;

public record HomeRecentCommentProperties(Duration candidateCacheTtl, int candidateSize) {
    public HomeRecentCommentProperties {
        if (candidateCacheTtl.isNegative() || candidateSize < 10) {
            throw new IllegalArgumentException("캐시 TTL과 후보 크기가 유효하지 않습니다.");
        }
    }
}
