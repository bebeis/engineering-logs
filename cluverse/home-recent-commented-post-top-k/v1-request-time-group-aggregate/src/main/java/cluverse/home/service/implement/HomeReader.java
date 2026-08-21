package cluverse.home.service.implement;

import cluverse.home.service.RecentCommentedPost;

import java.util.List;

public interface HomeReader {
    List<RecentCommentedPost> readRecentCommentedPosts(long memberId, int limit);
}
