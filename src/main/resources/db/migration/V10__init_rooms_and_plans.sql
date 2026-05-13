-- 1. BẢNG PHÒNG CHI TIẾT
CREATE TABLE homestay_rooms (
                                id BIGSERIAL PRIMARY KEY,
                                homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
                                name VARCHAR(255) NOT NULL,
                                description TEXT,
                                tag VARCHAR(50),        -- VD: 'Master Suite'
                                area VARCHAR(50),       -- VD: '45m2'
                                floor VARCHAR(50),      -- VD: 'Tầng 2'
                                wing VARCHAR(50),       -- VD: 'Cánh Tây'
                                max_guests INT NOT NULL DEFAULT 2,
                                bed_count INT NOT NULL DEFAULT 1,
                                quantity INT NOT NULL DEFAULT 1,
                                image_url VARCHAR(500),
                                status VARCHAR(50) DEFAULT 'ACTIVE',
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. BẢNG GÓI GIÁ
CREATE TABLE room_rate_plans (
                                 id BIGSERIAL PRIMARY KEY,
                                 room_id BIGINT NOT NULL REFERENCES homestay_rooms(id) ON DELETE CASCADE,
                                 name VARCHAR(255) NOT NULL,
                                 price DECIMAL(19, 2) NOT NULL, -- Chuẩn hóa Decimal(19,2)
                                 is_non_refundable BOOLEAN DEFAULT FALSE,
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. BẢNG LỊCH TRỐNG VÀ GIÁ BIẾN ĐỘNG
CREATE TABLE room_calendar (
                               id BIGSERIAL PRIMARY KEY,
                               room_id BIGINT NOT NULL REFERENCES homestay_rooms(id) ON DELETE CASCADE,
                               night_date DATE NOT NULL,
                               price_override DECIMAL(19, 2),
                               available_quantity INT,
                               CONSTRAINT uk_room_night UNIQUE (room_id, night_date)
);

-- 4. BẢNG TIỆN ÍCH NỔI BẬT CỦA PHÒNG (Hiện icon to)
CREATE TABLE room_amenity_highlights (
                                         room_id BIGINT NOT NULL REFERENCES homestay_rooms(id) ON DELETE CASCADE,
                                         amenity_id INT NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
                                         display_value VARCHAR(100), -- VD: '150 Mbps', 'View biển'
                                         PRIMARY KEY (room_id, amenity_id)
);

-- 5. BẢNG QUYỀN LỢI ĐI KÈM GÓI GIÁ (Hiện dấu tích xanh)
CREATE TABLE rate_plan_benefit_mapping (
                                           rate_plan_id BIGINT NOT NULL REFERENCES room_rate_plans(id) ON DELETE CASCADE,
                                           amenity_id INT NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
                                           PRIMARY KEY (rate_plan_id, amenity_id)
);