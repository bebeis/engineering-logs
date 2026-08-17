package cluverse.meta.service.implement;

import cluverse.meta.properties.ViewCountProperties;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LocalViewCountFallback {

    private final PostMetaReader postMetaReader;
    private final Map<Long, AtomicLong> deltas = new ConcurrentHashMap<>();
    private final Map<Long, Long> bases = new ConcurrentHashMap<>();
    private final Cache<String, Boolean> duplicateLocks;

    public LocalViewCountFallback(PostMetaReader postMetaReader, ViewCountProperties properties) {
        this.postMetaReader = postMetaReader;
        this.duplicateLocks = Caffeine.newBuilder()
                .maximumSize(1_000_000)
                .expireAfterWrite(properties.duplicateTtl())
                .build();
    }

    public ViewCountResult count(Long postId, String cookieId) {
        boolean counted = duplicateLocks.asMap()
                .putIfAbsent(postId + ":" + cookieId, Boolean.TRUE) == null;
        AtomicLong delta = deltas.computeIfAbsent(postId, ignored -> new AtomicLong());
        if (counted) {
            delta.incrementAndGet();
        }
        long base = bases.computeIfAbsent(postId, postMetaReader::readViewCount);
        return new ViewCountResult(base + delta.get(), counted, ViewCountSource.LOCAL_FALLBACK);
    }

    public Map<Long, AtomicLong> deltas() {
        return Map.copyOf(deltas);
    }

    public void reflected(Long postId) {
        bases.remove(postId);
        AtomicLong delta = deltas.get(postId);
        if (delta != null && delta.get() == 0) {
            deltas.remove(postId, delta);
        }
    }
}
