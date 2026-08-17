package cluverse.post.repository;

import cluverse.post.domain.PostStatus;
import cluverse.post.repository.dto.LatestPostEntry;
import cluverse.post.repository.dto.PostIdSlice;
import cluverse.post.service.request.PostListRequest;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static cluverse.post.domain.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostPageQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    /**
     * OFFSET 이동은 projection JOIN 전에 커버링 인덱스의 ID만 읽는다.
     * size + 1건을 조회해 별도 COUNT 없이 다음 페이지 존재 여부를 판단한다.
     */
    public PostIdSlice findPageIds(PostListRequest request) {
        int size = request.sizeOrDefault();
        List<Long> fetchedIds = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        post.boardId.eq(request.boardId()),
                        post.status.eq(PostStatus.ACTIVE)
                )
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(request.offset())
                .limit(size + 1L)
                .fetch();

        boolean hasNext = fetchedIds.size() > size;
        return new PostIdSlice(hasNext ? fetchedIds.subList(0, size) : fetchedIds, hasNext);
    }

    public List<LatestPostEntry> findLatestEntries(Long boardId, int limit) {
        return queryFactory
                .select(Projections.constructor(LatestPostEntry.class, post.id, post.createdAt))
                .from(post)
                .where(
                        post.boardId.eq(boardId),
                        post.status.eq(PostStatus.ACTIVE)
                )
                .orderBy(post.createdAt.desc(), post.id.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * LIMIT을 포함한 파생 테이블은 derived merge 대상이 아니다.
     * 따라서 MySQL은 searchLimit에 도달하면 전체 게시글을 더 세지 않고 멈춘다.
     */
    public long countUpTo(Long boardId, long searchLimit) {
        Query query = entityManager.createNativeQuery("""
                SELECT COUNT(*)
                FROM (
                    SELECT post_id
                    FROM post
                    WHERE board_id = :boardId
                      AND status = 'ACTIVE'
                    LIMIT :searchLimit
                ) capped
                """)
                .setParameter("boardId", boardId)
                .setParameter("searchLimit", searchLimit);

        return ((Number) query.getSingleResult()).longValue();
    }
}
