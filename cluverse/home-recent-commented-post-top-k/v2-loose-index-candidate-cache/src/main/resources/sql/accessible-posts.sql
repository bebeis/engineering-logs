SELECT p.post_id, p.title
FROM post p
JOIN board b ON b.board_id = p.board_id
JOIN member author ON author.member_id = p.member_id
WHERE p.post_id IN (:candidatePostIds)
  AND p.status = 'ACTIVE'
  AND b.is_active = TRUE
  AND author.status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1 FROM block bl
      WHERE bl.blocker_id = :memberId AND bl.blocked_id = p.member_id
  )
  AND (b.board_type <> 'GROUP' OR b.board_id IN (:readableGroupBoardIds));
