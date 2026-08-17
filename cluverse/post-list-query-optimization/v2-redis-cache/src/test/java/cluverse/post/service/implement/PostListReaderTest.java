package cluverse.post.service.implement;

import cluverse.post.domain.PostSummary;
import cluverse.post.repository.PostListCacheRepository;
import cluverse.post.repository.PostPageQueryRepository;
import cluverse.post.repository.PostSummaryQueryRepository;
import cluverse.post.repository.dto.CachedPostIds;
import cluverse.post.repository.dto.LatestPostEntry;
import cluverse.post.repository.dto.PostIdSlice;
import cluverse.post.service.request.PostListRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PostListReaderTest {

    @Test
    void 캐시_적중이면_ID_선정과_상한_COUNT를_실행하지_않는다() {
        Fixture fixture = new Fixture();
        PostListRequest request = new PostListRequest(3L, 1, 2);
        when(fixture.cacheRepository.read(3L, 0L, 3))
                .thenReturn(Optional.of(new CachedPostIds(List.of(30L, 20L, 10L), 3L)));
        when(fixture.summaryRepository.findByIds(List.of(30L, 20L)))
                .thenReturn(List.of(post(30L), post(20L)));

        var result = fixture.reader.read(request, 21L);

        assertThat(result.posts()).extracting(PostSummary::postId).containsExactly(30L, 20L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.cappedCount()).isEqualTo(3L);
        verify(fixture.pageRepository, never()).findPageIds(request);
        verify(fixture.pageRepository, never()).countUpTo(3L, 21L);
    }

    @Test
    void Redis_장애는_기존_DB_경로로_폴백한다() {
        Fixture fixture = new Fixture();
        PostListRequest request = new PostListRequest(3L, 1, 2);
        when(fixture.cacheRepository.read(3L, 0L, 3))
                .thenThrow(new IllegalStateException("redis down"));
        when(fixture.pageRepository.findPageIds(request))
                .thenReturn(new PostIdSlice(List.of(30L, 20L), true));
        when(fixture.summaryRepository.findByIds(List.of(30L, 20L)))
                .thenReturn(List.of(post(30L), post(20L)));
        when(fixture.pageRepository.countUpTo(3L, 21L)).thenReturn(21L);

        var result = fixture.reader.read(request, 21L);

        assertThat(result.posts()).hasSize(2);
        assertThat(result.cappedCount()).isEqualTo(21L);
    }

    @Test
    void 첫_미스는_버전이_바뀌지_않았을_때만_최신_ID를_적재한다() {
        Fixture fixture = new Fixture();
        PostListRequest request = new PostListRequest(3L, 1, 2);
        LocalDateTime now = LocalDateTime.of(2026, 8, 6, 12, 0);
        List<LatestPostEntry> entries = List.of(
                new LatestPostEntry(30L, now),
                new LatestPostEntry(20L, now.minusSeconds(1)),
                new LatestPostEntry(10L, now.minusSeconds(2))
        );
        when(fixture.cacheRepository.read(3L, 0L, 3)).thenReturn(Optional.empty());
        when(fixture.cacheRepository.tryAcquireWarmupLock(3L, "owner", fixture.policy.lockLease()))
                .thenReturn(true);
        when(fixture.cacheRepository.readVersion(3L)).thenReturn(4L);
        when(fixture.pageRepository.findLatestEntries(3L, fixture.policy.maxEntries()))
                .thenReturn(entries);
        when(fixture.cacheRepository.replaceIfVersion(3L, 4L, entries, fixture.policy.ttl()))
                .thenReturn(true);
        when(fixture.summaryRepository.findByIds(List.of(30L, 20L)))
                .thenReturn(List.of(post(30L), post(20L)));

        var result = fixture.reader.read(request, 21L, "owner");

        assertThat(result.posts()).hasSize(2);
        assertThat(result.cappedCount()).isEqualTo(3L);
        verify(fixture.cacheRepository).releaseWarmupLock(3L, "owner");
    }

    private PostSummary post(Long postId) {
        return new PostSummary(postId, "제목", "미리보기", 1L,
                LocalDateTime.of(2026, 8, 6, 12, 0));
    }

    private static class Fixture {
        private final PostListCacheRepository cacheRepository = mock(PostListCacheRepository.class);
        private final PostPageQueryRepository pageRepository = mock(PostPageQueryRepository.class);
        private final PostSummaryQueryRepository summaryRepository = mock(PostSummaryQueryRepository.class);
        private final PostListCachePolicy policy = PostListCachePolicy.defaults();
        private final PostListReader reader = new PostListReader(
                cacheRepository,
                pageRepository,
                summaryRepository,
                policy
        );
    }
}
