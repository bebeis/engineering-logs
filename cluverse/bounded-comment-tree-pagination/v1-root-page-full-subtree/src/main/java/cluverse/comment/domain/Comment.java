package cluverse.comment.domain;

public class Comment {
    private final long id;
    private final long authorId;
    private final Long parentId;
    private boolean deleted;

    public Comment(long id, long authorId, Long parentId) {
        this.id = id;
        this.authorId = authorId;
        this.parentId = parentId;
    }

    public long id() { return id; }
    public long authorId() { return authorId; }
    public Long parentId() { return parentId; }
    public boolean deleted() { return deleted; }
    public void deleteSoftly() { deleted = true; }
}
