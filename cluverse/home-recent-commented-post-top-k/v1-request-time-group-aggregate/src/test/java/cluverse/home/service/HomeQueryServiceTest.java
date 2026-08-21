package cluverse.home.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeQueryServiceTest {

    @Test
    void 홈_컴포넌트는_10건을_요청하지만_집계_범위는_제한하지_못한다() {
        AtomicInteger requestedLimit = new AtomicInteger();
        HomeQueryService service = new HomeQueryService((memberId, limit) -> {
            requestedLimit.set(limit);
            return List.of(new RecentCommentedPost(
                    1, "최근 댓글 글", LocalDateTime.of(2026, 5, 29, 10, 0)));
        });

        List<RecentCommentedPost> result = service.readRecentCommentedPosts(1);

        assertEquals(10, requestedLimit.get());
        assertEquals(1, result.size());
    }
}
