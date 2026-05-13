CREATE TABLE tour_images (
                             id BIGSERIAL PRIMARY KEY,
                             tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
                             image_url TEXT NOT NULL,
                             is_primary BOOLEAN DEFAULT FALSE,
                             display_order INT DEFAULT 0
);

CREATE TABLE tour_highlights_catalog (
                                         id SERIAL PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL UNIQUE, -- Ví dụ: 'Hướng dẫn viên', 'Phương tiện'
                                         icon_key VARCHAR(50), -- Ví dụ: 'psychology', 'directions_car'
                                         default_description TEXT -- Mô tả mặc định nếu chủ tour không nhập gì thêm
);

CREATE TABLE tour_highlight_mappings (
                                         tour_id BIGINT REFERENCES tours(id) ON DELETE CASCADE,
                                         highlight_id INT REFERENCES tour_highlights_catalog(id) ON DELETE CASCADE,

                                         custom_description TEXT,

                                         is_included BOOLEAN DEFAULT TRUE,
                                         display_order INT DEFAULT 0,

                                         PRIMARY KEY (tour_id, highlight_id)
);

CREATE INDEX idx_tour_highlights_lookup ON tour_highlight_mappings(tour_id);