package cluverse.post.service.implement;

import cluverse.post.domain.PostPageData;
import cluverse.post.domain.PostSummary;
import cluverse.post.repository.PostListCacheRepository;
import cluverse.post.repository.PostPageQueryRepository;
import cluverse.post.repository.PostSummaryQueryRepository;
import cluverse.post.repository.dto.CachedPostIds;
import cluverse.post.repository.dto.LatestPostEntry;
import cluverse.post.repository.dto.PostIdSlice;
import cluverse.post.service.request.PostListRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostListReader {

    private final PostListCacheRepository cacheRepository;
    private final PostPageQueryRepository pageRepository;
    private final PostSummaryQueryRepository summaryRepository;
    private final PostListCachePolicy cachePolicy;

    public PostPageData read(PostListRequest request, long searchLimit) {
        return read(request, searchLimit, UUID.randomUUID().toString());
    }

    PostPageData read(PostListRequest request, long searchLimit, String lockOwner) {
        if (!cachePolicy.supports(request, searchLimit)) {
            return readFromDatabase(request, searchLimit);
        }

        Optional<CachedPostIds> cached;
        try {
            cached = cacheRepository.read(
                    request.boardId(),
                    request.offset(),
                    request.sizeOrDefault() + 1
            );
        } catch (RuntimeException exception) {
            return fallbackAfterCacheFailure(request, searchLimit, exception);
        }

        if (cached.isPresent()) {
            PostPageData result = projectCachedIds(request, cached.get());
            return result != null ? result : readFromDatabase(request, searchLimit);
        }
        return warmCacheOrFallback(request, searchLimit, lockOwner);
    }

    private PostPageData warmCacheOrFallback(
            PostListRequest request,
            long searchLimit,
            String lockOwner
    ) {
        boolean acquired;
        try {
            acquired = cacheRepository.tryAcquireWarmupLock(
                    request.boardId(), lockOwner, cachePolicy.lockLease());
        } catch (RuntimeException exception) {
            return fallbackAfterCacheFailure(request, searchLimit, exception);
        }
        if (!acquired) {
            return readFromDatabase(request, searchLimit);
        }

        try {
            long version;
            try {
                version = cacheRepository.readVersion(request.boardId());
            } catch (RuntimeException exception) {
                return fallbackAfterCacheFailure(request, searchLimit, exception);
            }

            List<LatestPostEntry> entries = pageRepository.findLatestEntries(
                    request.boardId(), cachePolicy.maxEntries());
            try {
                boolean stored = cacheRepository.replaceIfVersion(
                        request.boardId(), version, entries, cachePolicy.ttl());
                if (!stored) {
                    return readFromDatabase(request, searchLimit);
                }
            } catch (RuntimeException exception) {
                return fallbackAfterCacheFailure(request, searchLimit, exception);
            }
            return projectEntries(request, entries, searchLimit);
        } finally {
            releaseLockSafely(request.boardId(), lockOwner);
        }
    }

    private PostPageData projectCachedIds(PostListRequest request, CachedPostIds cached) {
        List<Long> pageIds = cached.postIds().stream()
                .limit(request.sizeOrDefault())
                .toList();
        List<PostSummary> posts = summaryRepository.findByIds(pageIds);
        if (posts.size() != pageIds.size()) {
            invalidateSafely(request.boardId());
            return null;
        }
        return new PostPageData(
                posts,
                cached.postIds().size() > request.sizeOrDefault(),
                cached.cachedCount()
        );
    }

    private PostPageData projectEntries(
            PostListRequest request,
            List<LatestPostEntry> entries,
            long searchLimit
    ) {
        int fromIndex = (int) Math.min(request.offset(), entries.size());
        int toIndex = Math.min(fromIndex + request.sizeOrDefault() + 1, entries.size());
        List<Long> fetchedIds = entries.subList(fromIndex, toIndex).stream()
                .map(LatestPostEntry::postId)
                .toList();
        List<Long> pageIds = fetchedIds.stream().limit(request.sizeOrDefault()).toList();
        List<PostSummary> posts = summaryRepository.findByIds(pageIds);
        if (posts.size() != pageIds.size()) {
            invalidateSafely(request.boardId());
            return readFromDatabase(request, searchLimit);
        }

        return new PostPageData(
                posts,
                fetchedIds.size() > request.sizeOrDefault(),
                entries.size()
        );
    }

    private PostPageData readFromDatabase(PostListRequest request, long searchLimit) {
        PostIdSlice slice = pageRepository.findPageIds(request);
        return new PostPageData(
                summaryRepository.findByIds(slice.postIds()),
                slice.hasNext(),
                pageRepository.countUpTo(request.boardId(), searchLimit)
        );
    }

    private PostPageData fallbackAfterCacheFailure(
            PostListRequest request,
            long searchLimit,
            RuntimeException exception
    ) {
        log.warn("게시글 목록 캐시를 사용할 수 없어 DB로 조회합니다. boardId={}",
                request.boardId(), exception);
        return readFromDatabase(request, searchLimit);
    }

    private void invalidateSafely(Long boardId) {
        try {
            cacheRepository.invalidate(boardId);
        } catch (RuntimeException exception) {
            log.warn("오래된 목록 캐시 무효화에 실패했습니다. TTL 만료로 복구합니다. boardId={}",
                    boardId, exception);
        }
    }

    private void releaseLockSafely(Long boardId, String lockOwner) {
        try {
            cacheRepository.releaseWarmupLock(boardId, lockOwner);
        } catch (RuntimeException exception) {
            log.warn("캐시 워밍 락 해제에 실패했습니다. lease 만료를 기다립니다. boardId={}",
                    boardId, exception);
        }
    }
}
