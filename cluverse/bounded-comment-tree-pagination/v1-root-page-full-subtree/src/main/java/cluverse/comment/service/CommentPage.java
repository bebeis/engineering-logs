package cluverse.comment.service;

import cluverse.comment.domain.CommentView;

import java.util.List;

public record CommentPage(List<CommentView> comments, int offset, int limit, boolean hasNext) {
    public CommentPage {
        comments = List.copyOf(comments);
    }
}
