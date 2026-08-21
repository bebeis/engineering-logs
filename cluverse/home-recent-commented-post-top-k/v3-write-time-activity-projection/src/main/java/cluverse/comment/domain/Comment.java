package cluverse.comment.domain;

import java.time.LocalDateTime;

public class Comment {
    private final long id;
    private final long postId;
    private final LocalDateTime createdAt;
    private boolean deleted;

    public Comment(long id, long postId, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.createdAt = createdAt;
    }

    public long id() { return id; }
    public long postId() { return postId; }
    public LocalDateTime createdAt() { return createdAt; }
    public boolean deleted() { return deleted; }
    public LatestCommentKey latestKey() { return new LatestCommentKey(createdAt, id); }
    public void delete() { deleted = true; }
}
