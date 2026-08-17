package cluverse.meta.repository;

import cluverse.meta.repository.dto.ViewCountDelta;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@RequiredArgsConstructor
public class PostViewCountRepositoryImpl implements PostViewCountRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void increaseByDeltas(List<ViewCountDelta> deltas) {
        if (deltas.isEmpty()) {
            return;
        }
        jdbcTemplate.batchUpdate(
                "UPDATE post_view_count SET view_count = view_count + ?, updated_at = NOW() WHERE post_id = ?",
                deltas,
                Math.min(deltas.size(), 1_000),
                (statement, delta) -> {
                    statement.setLong(1, delta.delta());
                    statement.setLong(2, delta.postId());
                }
        );
    }
}
