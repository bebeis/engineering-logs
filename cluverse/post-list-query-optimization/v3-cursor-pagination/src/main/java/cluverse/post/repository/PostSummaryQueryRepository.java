package cluverse.post.repository;

import cluverse.post.domain.PostSummary;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cluverse.post.domain.QPost.post;
import static java.util.stream.Collectors.toMap;

@Repository
@RequiredArgsConstructor
public class PostSummaryQueryRepository {

    private static final int CONTENT_PREVIEW_LENGTH = 120;

    private final JPAQueryFactory queryFactory;

    public List<PostSummary> findByIds(List<Long> postIds) {
        if (postIds.isEmpty()) {
            return List.of();
        }

        List<PostSummary> rows = queryFactory
                .select(Projections.constructor(
                        PostSummary.class,
                        post.id,
                        post.title,
                        Expressions.stringTemplate(
                                "substring({0}, 1, {1})",
                                post.content,
                                Expressions.constant(CONTENT_PREVIEW_LENGTH)
                        ),
                        post.createdAt
                ))
                .from(post)
                .where(post.id.in(postIds))
                .fetch();

        Map<Long, PostSummary> rowById = rows.stream()
                .collect(toMap(PostSummary::postId, Function.identity()));
        return postIds.stream()
                .filter(rowById::containsKey)
                .map(rowById::get)
                .toList();
    }
}
