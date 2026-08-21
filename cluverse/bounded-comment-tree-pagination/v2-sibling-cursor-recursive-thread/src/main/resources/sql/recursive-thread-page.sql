WITH RECURSIVE comment_tree (comment_id, parent_id, depth, sort_path) AS (
    SELECT c.comment_id, c.parent_id, c.depth,
           CAST(CONCAT(DATE_FORMAT(c.created_at, '%Y%m%d%H%i%s%f'), '-',
                       LPAD(CAST(c.comment_id AS CHAR), 20, '0')) AS CHAR(512))
    FROM comment c
    WHERE c.comment_id = :rootCommentId

    UNION ALL

    SELECT child.comment_id, child.parent_id, child.depth,
           CONCAT(tree.sort_path, '/', DATE_FORMAT(child.created_at, '%Y%m%d%H%i%s%f'), '-',
                  LPAD(CAST(child.comment_id AS CHAR), 20, '0'))
    FROM comment child
    JOIN comment_tree tree ON child.parent_id = tree.comment_id
    WHERE child.post_id = :postId AND child.depth <= :maxDepth
)
SELECT comment_id, parent_id, depth, sort_path
FROM comment_tree
WHERE sort_path > :afterSortPath
ORDER BY sort_path
LIMIT :limitPlusOne;
