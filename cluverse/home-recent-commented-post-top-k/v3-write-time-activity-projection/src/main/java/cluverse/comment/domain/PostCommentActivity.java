package cluverse.comment.domain;

public class PostCommentActivity {
    private final long postId;
    private LatestCommentKey latest;

    public PostCommentActivity(long postId, LatestCommentKey latest) {
        this.postId = postId;
        this.latest = latest;
    }

    public void replaceLatest(Comment comment) {
        if (postId != comment.postId()) {
            throw new IllegalArgumentException("같은 게시글의 댓글만 반영할 수 있습니다.");
        }
        latest = comment.latestKey();
    }

    public long postId() { return postId; }
    public LatestCommentKey latest() { return latest; }
}
