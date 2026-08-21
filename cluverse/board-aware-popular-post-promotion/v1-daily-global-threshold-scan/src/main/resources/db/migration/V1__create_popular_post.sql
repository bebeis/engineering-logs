CREATE TABLE popular_post (
    popular_post_id BIGINT NOT NULL AUTO_INCREMENT,
    algorithm_version VARCHAR(8) NOT NULL,
    post_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    promoted_at DATETIME(6) NOT NULL,
    finalize_at DATETIME(6) NOT NULL,
    score_at_promotion BIGINT NOT NULL,
    promotion_score_threshold BIGINT NOT NULL,
    PRIMARY KEY (popular_post_id),
    CONSTRAINT uk_popular_post_version_post UNIQUE (algorithm_version, post_id)
);
