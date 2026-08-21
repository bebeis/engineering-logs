package cluverse.comment.service.implement;

import cluverse.comment.domain.CommentView;

import java.util.List;

public interface CommentReader {

    List<Long> readRootIds(long postId, int offset, int limitPlusOne);

    List<CommentView> readWholeSubtrees(long viewerId, List<Long> rootIds, int maxDepth);
}
