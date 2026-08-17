package cluverse.meta.repository;

import cluverse.meta.repository.dto.ViewCountSnapshot;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostViewCountRepository {

    private static final int MAX_BATCH_SIZE = 1_000;

    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    public long readViewCount(Long postId) {
        Object value = entityManager.createNativeQuery(
                        "SELECT view_count FROM post_view_count WHERE post_id = :postId")
                .setParameter("postId", postId)
                .getSingleResult();
        return ((Number) value).longValue();
    }

    public void checkpointViewCounts(List<ViewCountSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                """
                UPDATE post_view_count
                SET view_count = GREATEST(view_count, ?), updated_at = NOW()
                WHERE post_id = ?
                """,
                snapshots,
                Math.min(snapshots.size(), MAX_BATCH_SIZE),
                (statement, snapshot) -> {
                    statement.setLong(1, snapshot.viewCount());
                    statement.setLong(2, snapshot.postId());
                }
        );
    }
}
