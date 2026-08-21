package cluverse.comment.service;

import cluverse.comment.domain.Comment;

import java.util.List;

public record CommentPage(List<Comment> comments, String nextPath, boolean hasNext) {
    public CommentPage { comments = List.copyOf(comments); }
}
