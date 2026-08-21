package cluverse.home.service.implement;

import cluverse.home.domain.RecentCommentCandidate;
import cluverse.home.domain.RecentCommentedPost;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeReaderTest {

    @Test
    void 전역_후보는_캐시하고_사용자별_접근권한은_매번_검사한다() {
        AtomicInteger candidateReads = new AtomicInteger();
        AtomicInteger accessReads = new AtomicInteger();
        RecentCommentCandidateReader candidates = new RecentCommentCandidateReader() {
            public List<RecentCommentCandidate> readGlobalCandidates(int limit) {
                candidateReads.incrementAndGet();
                return List.of(candidate(3), candidate(2), candidate(1));
            }
            public List<RecentCommentedPost> readAccessibleFallback(long memberId, int limit) {
                return List.of();
            }
        };
        PostAccessReader access = (memberId, ids) -> {
            accessReads.incrementAndGet();
            return memberId == 1 ? Map.of(3L, "글 3", 1L, "글 1") : Map.of(2L, "글 2");
        };
        HomeReader reader = reader(candidates, access, 10);

        List<RecentCommentedPost> first = reader.readRecentCommentedPosts(1, 10);
        List<RecentCommentedPost> second = reader.readRecentCommentedPosts(2, 10);

        assertEquals(List.of(3L, 1L), first.stream().map(RecentCommentedPost::postId).toList());
        assertEquals(List.of(2L), second.stream().map(RecentCommentedPost::postId).toList());
        assertEquals(1, candidateReads.get());
        assertEquals(2, accessReads.get());
    }

    @Test
    void 캐시_범위에서_10건을_채우지_못하면_정확한_전체_집계로_폴백한다() {
        AtomicInteger fallbackReads = new AtomicInteger();
        RecentCommentCandidateReader candidates = new RecentCommentCandidateReader() {
            public List<RecentCommentCandidate> readGlobalCandidates(int limit) {
                return java.util.stream.LongStream.rangeClosed(1, 11)
                        .mapToObj(HomeReaderTest::candidate).toList();
            }
            public List<RecentCommentedPost> readAccessibleFallback(long memberId, int limit) {
                fallbackReads.incrementAndGet();
                return List.of(new RecentCommentedPost(20, "폴백 글", time(20)));
            }
        };
        HomeReader reader = reader(candidates, (memberId, ids) -> Map.of(1L, "접근 가능 글"), 10);

        List<RecentCommentedPost> result = reader.readRecentCommentedPosts(1, 10);

        assertEquals(List.of(20L), result.stream().map(RecentCommentedPost::postId).toList());
        assertEquals(1, fallbackReads.get());
    }

    private HomeReader reader(
            RecentCommentCandidateReader candidates,
            PostAccessReader access,
            int candidateSize
    ) {
        return new HomeReader(
                candidates,
                access,
                new HomeRecentCommentProperties(Duration.ofMinutes(1), candidateSize),
                Clock.fixed(Instant.parse("2026-05-29T01:00:00Z"), ZoneOffset.UTC)
        );
    }

    private static RecentCommentCandidate candidate(long postId) {
        return new RecentCommentCandidate(postId, time(postId));
    }

    private static LocalDateTime time(long offset) {
        return LocalDateTime.of(2026, 5, 29, 10, 0).minusSeconds(offset);
    }
}
