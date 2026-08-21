CREATE TABLE board_popularity_policy (
    board_id BIGINT NOT NULL,
    promotion_score BIGINT NOT NULL,
    sample_size INT NOT NULL,
    policy_source VARCHAR(16) NOT NULL,
    computed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (board_id)
);

CREATE TABLE popular_post (
    popular_post_id BIGINT NOT NULL AUTO_INCREMENT,
    algorithm_version VARCHAR(8) NOT NULL,
    post_id BIGINT NOT NULL,
    board_id BIGINT NOT NULL,
    promoted_at DATETIME(6) NOT NULL,
    finalize_at DATETIME(6) NOT NULL,
    score_at_promotion BIGINT NOT NULL,
    like_count_at_promotion BIGINT NOT NULL,
    comment_count_at_promotion BIGINT NOT NULL,
    promotion_score_threshold BIGINT NOT NULL,
    final_score BIGINT NULL,
    final_like_count BIGINT NULL,
    final_comment_count BIGINT NULL,
    finalized_at DATETIME(6) NULL,
    PRIMARY KEY (popular_post_id),
    CONSTRAINT uk_popular_post_version_post UNIQUE (algorithm_version, post_id),
    INDEX idx_popular_finalize_due (finalized_at, finalize_at, post_id)
);
