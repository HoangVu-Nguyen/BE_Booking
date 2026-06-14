-- 1. Thêm các cột còn thiếu vào bảng homestay_rooms để khớp với Giao diện
ALTER TABLE homestay_rooms
    ADD COLUMN type VARCHAR(50), -- Lưu 'BEDROOM', 'LIVING_ROOM'...
    ADD COLUMN has_private_bathroom BOOLEAN DEFAULT FALSE;

-- 2. Tạo bảng lưu chi tiết từng loại giường trong phòng
CREATE TABLE room_beds (
                           id BIGSERIAL PRIMARY KEY,
                           room_id BIGINT NOT NULL REFERENCES homestay_rooms(id) ON DELETE CASCADE,
                           bed_type VARCHAR(50) NOT NULL, -- 'SINGLE', 'DOUBLE', 'KING'...
                           quantity INT DEFAULT 1
);
CREATE INDEX idx_room_beds_room_id ON room_beds(room_id);

-- 3. Tạo bảng lưu bộ sưu tập ảnh của từng phòng
CREATE TABLE room_images (
                             id BIGSERIAL PRIMARY KEY,
                             room_id BIGINT NOT NULL REFERENCES homestay_rooms(id) ON DELETE CASCADE,
                             image_url TEXT NOT NULL,
                             is_cover BOOLEAN DEFAULT FALSE,
                             display_order INT DEFAULT 0
);
CREATE INDEX idx_room_images_room_id ON room_images(room_id);