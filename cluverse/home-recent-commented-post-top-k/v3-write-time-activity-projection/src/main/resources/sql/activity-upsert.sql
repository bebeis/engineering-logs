UPDATE post_comment_activity
SET last_comment_id = :lastCommentId,
    last_commented_at = :lastCommentedAt,
    updated_at = CURRENT_TIMESTAMP
WHERE post_id = :postId
  AND (
      last_commented_at < :lastCommentedAt
      OR (last_commented_at = :lastCommentedAt AND last_comment_id < :lastCommentId)
  );

-- UPDATE가 0건이면 INSERT를 시도한다.
INSERT INTO post_comment_activity (
    post_id, last_comment_id, last_commented_at, created_at, updated_at
) VALUES (
    :postId, :lastCommentId, :lastCommentedAt, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- 동시 INSERT가 PK 충돌하면 위 조건부 UPDATE를 한 번 더 실행한다.
