package cluverse.home.service.implement;

import cluverse.home.domain.RecentCommentCandidate;
import cluverse.home.domain.RecentCommentedPost;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class HomeReader {
    private final RecentCommentCandidateReader candidateReader;
    private final PostAccessReader postAccessReader;
    private final HomeRecentCommentProperties properties;
    private final Clock clock;
    private volatile CandidateSnapshot cached;

    public HomeReader(
            RecentCommentCandidateReader candidateReader,
            PostAccessReader postAccessReader,
            HomeRecentCommentProperties properties,
            Clock clock
    ) {
        this.candidateReader = candidateReader;
        this.postAccessReader = postAccessReader;
        this.properties = properties;
        this.clock = clock;
    }

    public List<RecentCommentedPost> readRecentCommentedPosts(long memberId, int size) {
        CandidateSnapshot snapshot = readSnapshot();
        if (snapshot.candidates().isEmpty()) {
            return List.of();
        }
        List<Long> ids = snapshot.candidates().stream().map(RecentCommentCandidate::postId).toList();
        Map<Long, String> accessible = postAccessReader.readAccessibleTitles(memberId, ids);
        List<RecentCommentedPost> result = snapshot.candidates().stream()
                .filter(candidate -> accessible.containsKey(candidate.postId()))
                .limit(size)
                .map(candidate -> new RecentCommentedPost(
                        candidate.postId(), accessible.get(candidate.postId()), candidate.lastCommentedAt()))
                .toList();
        if (result.size() >= size || !snapshot.hasMore()) {
            return result;
        }
        return candidateReader.readAccessibleFallback(memberId, size);
    }

    private CandidateSnapshot readSnapshot() {
        CandidateSnapshot current = cached;
        Instant now = clock.instant();
        if (current != null && now.isBefore(current.loadedAt().plus(properties.candidateCacheTtl()))) {
            return current;
        }
        return refresh(now);
    }

    private synchronized CandidateSnapshot refresh(Instant now) {
        CandidateSnapshot current = cached;
        if (current != null && now.isBefore(current.loadedAt().plus(properties.candidateCacheTtl()))) {
            return current;
        }
        List<RecentCommentCandidate> selected = candidateReader.readGlobalCandidates(properties.candidateSize() + 1);
        boolean hasMore = selected.size() > properties.candidateSize();
        cached = new CandidateSnapshot(
                selected.stream().limit(properties.candidateSize()).toList(), hasMore, now);
        return cached;
    }

    private record CandidateSnapshot(
            List<RecentCommentCandidate> candidates,
            boolean hasMore,
            Instant loadedAt
    ) {
        private CandidateSnapshot { candidates = List.copyOf(candidates); }
    }
}
