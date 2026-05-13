CREATE TABLE tour_categories (
                                 id SERIAL PRIMARY KEY,
                                 name VARCHAR(100) NOT NULL UNIQUE,
                                 icon_url TEXT
);

-- 2. Bảng Tour lõi
CREATE TABLE tours (
                       id BIGSERIAL PRIMARY KEY,
                       homestay_id BIGINT NOT NULL REFERENCES homestays(id),
                       category_id INT REFERENCES tour_categories(id),
                       name VARCHAR(255) NOT NULL,
                       description TEXT,
                       location_detail TEXT,
                       duration_type VARCHAR(20) NOT NULL, -- 'HOURS', 'DAYS'
                       duration_value INT NOT NULL,
                       price_per_person DECIMAL(19, 2) NOT NULL,
                       max_participants INT NOT NULL DEFAULT 1,
                       allow_external_guests BOOLEAN DEFAULT FALSE,
                       status VARCHAR(50) DEFAULT 'ACTIVE',
                       version INT DEFAULT 0 NOT NULL,
                       deleted_at TIMESTAMP WITH TIME ZONE,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tour_availability (
                                   id BIGSERIAL PRIMARY KEY,
                                   tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
                                   start_date DATE NOT NULL,
                                   start_time TIME NOT NULL, -- Giờ khởi hành
                                   remaining_slots INT NOT NULL, -- Số chỗ còn trống thực tế
                                   price_override DECIMAL(19, 2), -- Giá riêng cho ngày lễ chẳng hạn
                                   is_active BOOLEAN DEFAULT TRUE,
                                   UNIQUE(tour_id, start_date, start_time)
);