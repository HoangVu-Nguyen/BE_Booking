CREATE TABLE homestays (
                           id BIGSERIAL PRIMARY KEY,
                           owner_id BIGINT NOT NULL,
                           category_id INT REFERENCES categories(id),
                           location_id INT REFERENCES locations(id),
                           name VARCHAR(255) NOT NULL,
                           description TEXT,
                           address_detail TEXT NOT NULL,
                           latitude DECIMAL(10, 8),
                           longitude DECIMAL(11, 8),
                           average_rating DECIMAL(3, 2) DEFAULT 0.00,
                           review_count INT DEFAULT 0,
                           status VARCHAR(50) DEFAULT 'AVAILABLE',
                           version INT DEFAULT 0 NOT NULL,
                           deleted_at TIMESTAMP WITH TIME ZONE,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. BẢNG HÌNH ẢNH & TIỆN ÍCH TỔNG QUAN
CREATE TABLE homestay_images (
                                 id BIGSERIAL PRIMARY KEY,
                                 homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
                                 image_url TEXT NOT NULL,
                                 is_primary BOOLEAN DEFAULT FALSE,
                                 display_order INT DEFAULT 0
);

CREATE TABLE homestay_amenities (
                                    id BIGSERIAL PRIMARY KEY,
                                    homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
                                    amenity_id INT NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
                                    CONSTRAINT uk_homestay_amenity UNIQUE (homestay_id, amenity_id)
);
CREATE INDEX idx_homestay_amenities_homestay_id ON homestay_amenities(homestay_id);

-- 4. BẢNG CHÍNH SÁCH NHẬN TRẢ PHÒNG
CREATE TABLE homestay_policies (
                                   id BIGSERIAL PRIMARY KEY,
                                   homestay_id BIGINT UNIQUE NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
                                   check_in_time TIME DEFAULT '14:00:00',
                                   check_out_time TIME DEFAULT '12:00:00',
                                   late_check_in_instruction TEXT,
                                   allows_pets BOOLEAN DEFAULT FALSE,
                                   allows_smoking BOOLEAN DEFAULT FALSE,
                                   allows_parties BOOLEAN DEFAULT FALSE
);