package cluverse.meta.repository;

import cluverse.meta.domain.PostViewCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostViewCountRepository extends JpaRepository<PostViewCount, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT postViewCount
            FROM PostViewCount postViewCount
            WHERE postViewCount.postId = :postId
            """)
    Optional<PostViewCount> findByPostIdForUpdate(@Param("postId") Long postId);
}
