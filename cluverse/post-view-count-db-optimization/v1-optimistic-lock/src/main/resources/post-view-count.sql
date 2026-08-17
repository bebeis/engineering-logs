CREATE TABLE post_view_count_optimistic (
    post_id BIGINT NOT NULL,
    view_count INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT NOW(),
    updated_at DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    PRIMARY KEY (post_id)
);
