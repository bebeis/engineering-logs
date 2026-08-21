package cluverse.home.service;

import cluverse.home.domain.RecentCommentedPost;
import cluverse.home.service.implement.HomeReader;

import java.util.List;

public class HomeQueryService {
    private static final int COMPONENT_SIZE = 10;
    private final HomeReader homeReader;

    public HomeQueryService(HomeReader homeReader) { this.homeReader = homeReader; }

    public List<RecentCommentedPost> readRecentCommentedPosts(long memberId) {
        return homeReader.readRecentCommentedPosts(memberId, COMPONENT_SIZE);
    }
}
