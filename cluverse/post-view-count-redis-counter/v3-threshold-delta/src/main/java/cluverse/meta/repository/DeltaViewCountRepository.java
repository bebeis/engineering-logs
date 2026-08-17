package cluverse.meta.repository;

import cluverse.meta.properties.ViewCountProperties;
import cluverse.meta.repository.dto.DeltaViewCountResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DeltaViewCountRepository {

    private static final String DELTA_KEY_PREFIX = "view:v3:delta:";
    private static final String DUPLICATE_KEY_PREFIX = "view:v3:dedupe:";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> countDeltaScript;
    private final RedisScript<Long> getAndDeleteScript;
    private final ViewCountProperties properties;

    public DeltaViewCountRepository(
            StringRedisTemplate redisTemplate,
            @Qualifier("countDeltaScript") RedisScript<List> countDeltaScript,
            @Qualifier("getAndDeleteScript") RedisScript<Long> getAndDeleteScript,
            ViewCountProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.countDeltaScript = countDeltaScript;
        this.getAndDeleteScript = getAndDeleteScript;
        this.properties = properties;
    }

    public DeltaViewCountResult count(Long postId, String cookieId) {
        List<?> values = redisTemplate.execute(
                countDeltaScript,
                List.of(duplicateKey(postId, cookieId), deltaKey(postId)),
                String.valueOf(properties.duplicateTtl().toSeconds())
        );
        if (values == null || values.size() != 2) {
            throw new IllegalStateException("Redis 증분 조회수 결과가 올바르지 않습니다.");
        }
        return new DeltaViewCountResult(asLong(values.get(0)) == 1L, asLong(values.get(1)));
    }

    public long take(Long postId) {
        Long delta = redisTemplate.execute(getAndDeleteScript, List.of(deltaKey(postId)));
        return delta == null ? 0L : delta;
    }

    public void restore(Long postId, long delta) {
        redisTemplate.opsForValue().increment(deltaKey(postId), delta);
    }

    private String deltaKey(Long postId) {
        return DELTA_KEY_PREFIX + "{" + postId + "}";
    }

    private String duplicateKey(Long postId, String cookieId) {
        return DUPLICATE_KEY_PREFIX + "{" + postId + "}:" + cookieId;
    }

    private long asLong(Object value) {
        return ((Number) value).longValue();
    }
}
