-- Thêm cột status vào bảng room_images
-- Lưu dưới dạng VARCHAR vì trong Entity đang dùng EnumType.STRING
ALTER TABLE room_images
    ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE';

