package cluverse.post.repository;

import cluverse.post.domain.PostStatus;
import cluverse.post.repository.dto.PostIdSlice;
import cluverse.post.service.request.PostCursorDirection;
import cluverse.post.service.request.PostCursorRequest;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cluverse.post.domain.QPost.post;

@Repository
@RequiredArgsConstructor
public class PostPageQueryRepository {

    private final JPAQueryFactory queryFactory;

    public PostIdSlice findPostIds(PostCursorRequest request) {
        int size = request.sizeOrDefault();
        boolean previous = request.isPreviousMove();

        List<Long> fetchedIds = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        post.boardId.eq(request.boardId()),
                        post.status.eq(PostStatus.ACTIVE),
                        cursorAnchor(request)
                )
                .orderBy(previous
                        ? new OrderSpecifier<?>[]{post.createdAt.asc(), post.id.asc()}
                        : new OrderSpecifier<?>[]{post.createdAt.desc(), post.id.desc()})
                .limit(size + 1L)
                .fetch();

        boolean hasMore = fetchedIds.size() > size;
        List<Long> pageIds = hasMore ? fetchedIds.subList(0, size) : fetchedIds;
        if (!previous) {
            return new PostIdSlice(pageIds, hasMore);
        }

        List<Long> latestFirst = new ArrayList<>(pageIds);
        Collections.reverse(latestFirst);
        return new PostIdSlice(latestFirst, hasMore);
    }

    public boolean existsNewerThan(Long boardId, LocalDateTime exclusiveDateEnd) {
        return queryFactory
                .selectOne()
                .from(post)
                .where(
                        post.boardId.eq(boardId),
                        post.status.eq(PostStatus.ACTIVE),
                        post.createdAt.goe(exclusiveDateEnd)
                )
                .fetchFirst() != null;
    }

    private BooleanExpression cursorAnchor(PostCursorRequest request) {
        if (request.hasCursor()) {
            LocalDateTime createdAt = request.cursorCreatedAt();
            Long postId = request.cursorPostId();
            if (request.directionOrDefault() == PostCursorDirection.PREV) {
                return post.createdAt.gt(createdAt)
                        .or(post.createdAt.eq(createdAt).and(post.id.gt(postId)));
            }
            return post.createdAt.lt(createdAt)
                    .or(post.createdAt.eq(createdAt).and(post.id.lt(postId)));
        }
        if (request.isDateAnchored()) {
            return post.createdAt.lt(request.exclusiveDateEnd());
        }
        return null;
    }
}
