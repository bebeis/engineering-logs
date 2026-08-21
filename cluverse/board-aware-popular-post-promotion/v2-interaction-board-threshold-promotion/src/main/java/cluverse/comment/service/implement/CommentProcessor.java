package cluverse.comment.service.implement;

public interface CommentProcessor {

    long create(long memberId, long postId, String content);
}
