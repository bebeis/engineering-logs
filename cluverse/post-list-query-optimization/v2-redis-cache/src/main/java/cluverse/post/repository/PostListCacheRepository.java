package cluverse.post.repository;

import cluverse.post.repository.dto.CachedPostIds;
import cluverse.post.repository.dto.LatestPostEntry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PostListCacheRepository {

    private static final String KEY_PREFIX = "post:list:latest:";
    private static final int POST_ID_WIDTH = 19;

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> readScript;
    private final RedisScript<Long> replaceScript;
    private final RedisScript<Long> invalidateScript;
    private final RedisScript<Long> unlockScript;
    private final Clock clock;

    public PostListCacheRepository(
            StringRedisTemplate redisTemplate,
            @Qualifier("readLatestPostIdsScript") RedisScript<List> readScript,
            @Qualifier("replaceLatestPostIdsScript") RedisScript<Long> replaceScript,
            @Qualifier("invalidateLatestPostIdsScript") RedisScript<Long> invalidateScript,
            @Qualifier("unlockScript") RedisScript<Long> unlockScript,
            Clock clock
    ) {
        this.redisTemplate = redisTemplate;
        this.readScript = readScript;
        this.replaceScript = replaceScript;
        this.invalidateScript = invalidateScript;
        this.unlockScript = unlockScript;
        this.clock = clock;
    }

    public Optional<CachedPostIds> read(Long boardId, long offset, int limit) {
        CacheKeys keys = keys(boardId);
        List<?> values = redisTemplate.execute(
                readScript,
                List.of(keys.ids(), keys.ready()),
                String.valueOf(offset),
                String.valueOf(offset + limit - 1)
        );
        if (values == null || values.isEmpty() || asLong(values.getFirst()) < 0) {
            return Optional.empty();
        }

        List<Long> postIds = values.stream()
                .skip(1)
                .map(value -> Long.valueOf(value.toString()))
                .toList();
        return Optional.of(new CachedPostIds(postIds, asLong(values.getFirst())));
    }

    public boolean tryAcquireWarmupLock(Long boardId, String owner, Duration lease) {
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(keys(boardId).lock(), owner, lease);
        return Boolean.TRUE.equals(acquired);
    }

    public void releaseWarmupLock(Long boardId, String owner) {
        redisTemplate.execute(unlockScript, List.of(keys(boardId).lock()), owner);
    }

    public long readVersion(Long boardId) {
        String version = redisTemplate.opsForValue().get(keys(boardId).version());
        return version == null ? 0L : Long.parseLong(version);
    }

    public boolean replaceIfVersion(
            Long boardId,
            long expectedVersion,
            List<LatestPostEntry> entries,
            Duration ttl
    ) {
        CacheKeys keys = keys(boardId);
        List<String> arguments = new ArrayList<>(2 + entries.size() * 2);
        arguments.add(String.valueOf(expectedVersion));
        arguments.add(String.valueOf(ttl.toSeconds()));
        for (LatestPostEntry entry : entries) {
            arguments.add(paddedPostId(entry.postId()));
            arguments.add(String.valueOf(entry.createdAt()
                    .atZone(clock.getZone())
                    .toInstant()
                    .toEpochMilli()));
        }

        Long replaced = redisTemplate.execute(
                replaceScript,
                List.of(keys.ids(), keys.ready(), keys.version()),
                arguments.toArray()
        );
        return replaced != null && replaced == 1L;
    }

    public void invalidate(Long boardId) {
        CacheKeys keys = keys(boardId);
        redisTemplate.execute(
                invalidateScript,
                List.of(keys.ids(), keys.ready(), keys.version())
        );
    }

    private CacheKeys keys(Long boardId) {
        String prefix = KEY_PREFIX + "{" + boardId + "}";
        return new CacheKeys(
                prefix + ":ids",
                prefix + ":ready",
                prefix + ":version",
                prefix + ":lock"
        );
    }

    private String paddedPostId(Long postId) {
        return String.format("%0" + POST_ID_WIDTH + "d", postId);
    }

    private long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : Long.parseLong(value.toString());
    }

    private record CacheKeys(String ids, String ready, String version, String lock) {
    }
}
