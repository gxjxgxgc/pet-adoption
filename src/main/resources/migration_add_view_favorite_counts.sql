ALTER TABLE pet_pet ADD COLUMN view_count INT DEFAULT 0 COMMENT '浏览量';
ALTER TABLE pet_pet ADD COLUMN favorite_count INT DEFAULT 0 COMMENT '收藏数';
ALTER TABLE pet_pet ADD INDEX idx_view_count (view_count);
ALTER TABLE pet_pet ADD INDEX idx_favorite_count (favorite_count);
