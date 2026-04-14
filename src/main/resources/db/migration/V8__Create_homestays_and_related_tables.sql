CREATE TABLE homestays (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           description TEXT,
                           address TEXT NOT NULL,
                           city VARCHAR(100) NOT NULL,


                           latitude DECIMAL(10, 8),
                           longitude DECIMAL(11, 8),


                           base_price DECIMAL(19, 2) NOT NULL,


                           max_guests INT NOT NULL DEFAULT 1,
                           num_bedrooms INT NOT NULL DEFAULT 1,
                           num_bathrooms INT NOT NULL DEFAULT 1,


                           average_rating DECIMAL(3, 2) DEFAULT 0.00,
                           review_count INT DEFAULT 0,

                           status VARCHAR(50) DEFAULT 'AVAILABLE',
                           owner_id BIGINT NOT NULL,

                           version INT DEFAULT 0 NOT NULL,
                           deleted_at TIMESTAMP WITH TIME ZONE,
                           created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tạo Index cho Rating để sau này bác làm tính năng "Sắp xếp theo đánh giá cao nhất"
CREATE INDEX idx_homestays_rating ON homestays(average_rating DESC);
CREATE TABLE homestay_images (
                                 id BIGSERIAL PRIMARY KEY,
                                 homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
                                 image_url TEXT NOT NULL,
                                 is_primary BOOLEAN DEFAULT FALSE,
                                 display_order INT DEFAULT 0,
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE amenities (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(100) NOT NULL UNIQUE,
                           icon_name VARCHAR(50)
);

CREATE TABLE homestay_amenities (
                                    homestay_id BIGINT REFERENCES homestays(id) ON DELETE CASCADE,
                                    amenity_id BIGINT REFERENCES amenities(id) ON DELETE CASCADE,
                                    PRIMARY KEY (homestay_id, amenity_id)
);