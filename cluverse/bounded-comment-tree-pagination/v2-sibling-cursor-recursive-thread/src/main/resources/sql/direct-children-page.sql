SELECT comment_id
FROM comment
WHERE post_id = :postId
  AND parent_id <=> :parentCommentId
  AND created_at <= :asOf
  AND comment_id <= :snapshotMaxCommentId
  AND (
      created_at > :cursorCreatedAt
      OR (created_at = :cursorCreatedAt AND comment_id > :cursorCommentId)
  )
ORDER BY created_at, comment_id
LIMIT :limitPlusOne;
