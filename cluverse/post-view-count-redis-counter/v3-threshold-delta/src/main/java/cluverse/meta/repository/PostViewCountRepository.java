package cluverse.meta.repository;

import cluverse.meta.domain.PostViewCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewCountRepository
        extends JpaRepository<PostViewCount, Long>, PostViewCountRepositoryCustom {
}
