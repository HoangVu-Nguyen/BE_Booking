-- Cập nhật bảng review_images để hỗ trợ upload ảnh pending
ALTER TABLE review_images ALTER COLUMN review_id DROP NOT NULL;
ALTER TABLE review_images ADD COLUMN guest_id BIGINT;
ALTER TABLE review_images ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';
