-- V58__update_tour_images_for_upload.sql
-- Thêm cơ chế PENDING cho bảng tour_images

-- 1. Xóa ràng buộc NOT NULL của tour_id
ALTER TABLE tour_images ALTER COLUMN tour_id DROP NOT NULL;

-- 2. Thêm cột status (mặc định là 'PENDING')
ALTER TABLE tour_images ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING';

-- 3. Thêm cột owner_id để đánh dấu ai là người đang upload
ALTER TABLE tour_images ADD COLUMN owner_id BIGINT;
