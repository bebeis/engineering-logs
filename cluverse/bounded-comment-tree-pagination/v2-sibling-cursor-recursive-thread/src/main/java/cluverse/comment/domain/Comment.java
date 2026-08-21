package cluverse.comment.domain;

import java.time.LocalDateTime;

public class Comment {
    private final long id;
    private final long postId;
    private final long authorId;
    private final Long parentId;
    private final int depth;
    private final LocalDateTime createdAt;
    private CommentStatus status = CommentStatus.ACTIVE;

    public Comment(long id, long postId, long authorId, Long parentId, int depth, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.parentId = parentId;
        this.depth = depth;
        this.createdAt = createdAt;
    }

    public long id() { return id; }
    public long postId() { return postId; }
    public long authorId() { return authorId; }
    public Long parentId() { return parentId; }
    public int depth() { return depth; }
    public LocalDateTime createdAt() { return createdAt; }
    public boolean active() { return status == CommentStatus.ACTIVE; }
    public boolean deleted() { return status == CommentStatus.DELETED; }
    public void deleteSoftly() { status = CommentStatus.DELETED; }
}
