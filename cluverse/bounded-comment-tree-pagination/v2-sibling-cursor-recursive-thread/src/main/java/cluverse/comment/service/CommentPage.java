package cluverse.comment.service;

import cluverse.comment.domain.Comment;
import cluverse.comment.domain.CommentCursor;

import java.util.List;

public record CommentPage(List<Comment> comments, CommentCursor nextCursor, boolean hasNext) {
    public CommentPage { comments = List.copyOf(comments); }
}
