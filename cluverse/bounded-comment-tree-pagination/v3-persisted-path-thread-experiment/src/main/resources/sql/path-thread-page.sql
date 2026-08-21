SELECT comment_id, parent_id, depth, path
FROM comment
WHERE post_id = :postId
  AND path LIKE CONCAT(:rootPath, '%')
  AND path > :afterPath
ORDER BY path
LIMIT :limitPlusOne;
