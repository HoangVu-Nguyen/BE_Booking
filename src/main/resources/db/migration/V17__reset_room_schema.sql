DROP TABLE IF EXISTS room_amenity_mapping; -- Xóa bảng cũ của bác
DROP TABLE IF EXISTS homestay_rooms;       -- Xóa bảng cũ của bác

--- BƯỚC 2: TẠO HỆ THỐNG BẢNG MỚI (LUXURY VERSION) ---

-- 1. Bảng xác phòng
CREATE TABLE homestay_rooms (
                                id BIGSERIAL PRIMARY KEY,
                                homestay_id BIGINT NOT NULL,
                                name VARCHAR(255) NOT NULL,
                                description TEXT,
                                tag VARCHAR(50),        -- 'Master Suite'
                                area VARCHAR(50),       -- '45m2'
                                floor VARCHAR(50),      -- 'Tầng 2'
                                wing VARCHAR(50),       -- 'Cánh Tây'
                                max_guests INT NOT NULL DEFAULT 2,
                                bed_count INT NOT NULL DEFAULT 1,
                                quantity INT NOT NULL DEFAULT 1,
                                image_url VARCHAR(500),
                                status VARCHAR(50) DEFAULT 'ACTIVE',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_homestay FOREIGN KEY (homestay_id) REFERENCES homestays(id) ON DELETE CASCADE
);

-- 2. Bảng các gói giá (Để hiện nhiều mức giá cho 1 phòng)
CREATE TABLE room_rate_plans (
                                 id BIGSERIAL PRIMARY KEY,
                                 room_id BIGINT NOT NULL,
                                 name VARCHAR(255) NOT NULL, -- 'Gói Cơ bản', 'Gói Cao cấp'
                                 price DECIMAL(15, 2) NOT NULL,
                                 is_non_refundable BOOLEAN DEFAULT FALSE,
                                 CONSTRAINT fk_rate_room FOREIGN KEY (room_id) REFERENCES homestay_rooms(id) ON DELETE CASCADE
);

-- 3. Bảng chọn 6 tiện ích nổi bật (Hiện icon to ở FE)
CREATE TABLE room_amenity_highlights (
                                         room_id BIGINT NOT NULL,
                                         amenity_id INT NOT NULL,
                                         display_value VARCHAR(100), -- '150 Mbps', 'View biển'
                                         PRIMARY KEY (room_id, amenity_id),
                                         CONSTRAINT fk_highlight_room FOREIGN KEY (room_id) REFERENCES homestay_rooms(id) ON DELETE CASCADE,
                                         CONSTRAINT fk_highlight_amenity FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
);

-- 4. Bảng chọn tiện ích cho từng gói giá (Hiện dấu tích xanh)
CREATE TABLE rate_plan_benefit_mapping (
                                           rate_plan_id BIGINT NOT NULL,
                                           amenity_id INT NOT NULL,
                                           PRIMARY KEY (rate_plan_id, amenity_id),
                                           CONSTRAINT fk_benefit_plan FOREIGN KEY (rate_plan_id) REFERENCES room_rate_plans(id) ON DELETE CASCADE,
                                           CONSTRAINT fk_benefit_amenity FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
);