package cluverse.post.repository;

import cluverse.post.domain.PostSummary;
import cluverse.post.domain.PostStatus;
import cluverse.post.service.request.PostListRequest;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static cluverse.member.domain.QMember.member;
import static cluverse.meta.domain.QPostCommentCount.postCommentCount;
import static cluverse.meta.domain.QPostLikeCount.postLikeCount;
import static cluverse.meta.domain.QPostViewCount.postViewCount;
import static cluverse.post.domain.QPost.post;
import static cluverse.post.domain.QPostImage.postImage;

@Repository
@RequiredArgsConstructor
public class PostListQueryRepository {

    private static final int CONTENT_PREVIEW_LENGTH = 120;

    private final JPAQueryFactory queryFactory;

    /**
     * 개선 전 기준선이다. OFFSET에 도달하기 전에 projection과 모든 JOIN이 적용되므로,
     * 버려지는 행도 클러스터드 인덱스 조회와 조인 비용을 지불한다.
     */
    public List<PostSummary> findSummariesWithOffset(PostListRequest request) {
        return queryFactory
                .select(Projections.constructor(
                        PostSummary.class,
                        post.id,
                        post.title,
                        Expressions.stringTemplate(
                                "substring({0}, 1, {1})",
                                post.content,
                                Expressions.constant(CONTENT_PREVIEW_LENGTH)
                        ),
                        postImage.imageUrl,
                        postViewCount.viewCount.coalesce(0L),
                        postLikeCount.likeCount.coalesce(0L),
                        postCommentCount.commentCount.coalesce(0L),
                        member.nickname,
                        post.createdAt
                ))
                .from(post)
                .leftJoin(postImage).on(postImage.post.eq(post), postImage.displayOrder.eq(0))
                .leftJoin(postViewCount).on(postViewCount.postId.eq(post.id))
                .leftJoin(postLikeCount).on(postLikeCount.postId.eq(post.id))
                .leftJoin(postCommentCount).on(postCommentCount.postId.eq(post.id))
                .join(member).on(member.id.eq(post.memberId))
                .where(
                        post.boardId.eq(request.boardId()),
                        post.status.eq(PostStatus.ACTIVE)
                )
                .orderBy(post.createdAt.desc(), post.id.desc())
                .offset(request.offset())
                .limit(request.sizeOrDefault())
                .fetch();
    }

    public long countActivePosts(Long boardId) {
        Long count = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.boardId.eq(boardId),
                        post.status.eq(PostStatus.ACTIVE)
                )
                .fetchOne();
        return count == null ? 0L : count;
    }
}
