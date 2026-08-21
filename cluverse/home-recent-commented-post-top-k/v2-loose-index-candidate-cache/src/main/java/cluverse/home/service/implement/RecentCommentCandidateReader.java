package cluverse.home.service.implement;

import cluverse.home.domain.RecentCommentCandidate;
import cluverse.home.domain.RecentCommentedPost;

import java.util.List;

public interface RecentCommentCandidateReader {
    List<RecentCommentCandidate> readGlobalCandidates(int limitPlusOne);
    List<RecentCommentedPost> readAccessibleFallback(long memberId, int limit);
}
