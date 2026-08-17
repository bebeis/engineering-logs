CREATE INDEX idx_post_board_status_created_id
    ON post (board_id, status, created_at DESC, post_id DESC);
