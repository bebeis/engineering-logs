package cluverse.comment.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Comment {
    private static final DateTimeFormatter PATH_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final int MAX_PATH_LENGTH = 255;

    private final long id;
    private final long postId;
    private final Long parentId;
    private final int depth;
    private final LocalDateTime createdAt;
    private String path;

    public Comment(long id, long postId, Long parentId, int depth, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.parentId = parentId;
        this.depth = depth;
        this.createdAt = createdAt;
    }

    public void assignPath(Comment parent) {
        String segment = PATH_TIME.format(createdAt) + "-" + String.format(Locale.ROOT, "%020d", id);
        String generated = parent == null ? segment : requireParentPath(parent) + "/" + segment;
        if (generated.length() > MAX_PATH_LENGTH) {
            throw new IllegalStateException("댓글 path 길이를 초과했습니다.");
        }
        path = generated;
    }

    private String requireParentPath(Comment parent) {
        if (parent.path == null || parent.path.isBlank()) {
            throw new IllegalStateException("부모 댓글의 path가 필요합니다.");
        }
        return parent.path;
    }

    public long id() { return id; }
    public long postId() { return postId; }
    public Long parentId() { return parentId; }
    public int depth() { return depth; }
    public LocalDateTime createdAt() { return createdAt; }
    public String path() { return path; }
}
