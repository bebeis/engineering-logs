CREATE INDEX idx_comment_sibling_page
    ON comment (post_id, parent_id, created_at, comment_id);

ALTER TABLE comment
    ADD CONSTRAINT fk_comment_parent
    FOREIGN KEY (parent_id) REFERENCES comment (comment_id);

-- CommentStore.lock은 아래 잠금을 사용한다.
SELECT comment_id, post_id, status, depth
FROM comment
WHERE comment_id = :commentId
FOR UPDATE;
