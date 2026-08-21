ALTER TABLE comment
    ADD COLUMN path VARCHAR(255)
        CHARACTER SET ascii
        COLLATE ascii_bin
        NULL;

-- 신규 쓰기에 path를 적용한 뒤 depth 순서로 작은 배치를 백필한다.
-- 모든 행의 path 불변식을 확인한 다음 NOT NULL로 전환한다.
CREATE INDEX idx_comment_post_path ON comment (post_id, path);
