
CREATE TABLE tour_search_index (
                                   tour_id BIGINT PRIMARY KEY REFERENCES tours(id) ON DELETE CASCADE,
                                   homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,

    -- Dữ liệu lọc cứng (Hard Filters)
                                   category_name VARCHAR(100),
                                   name VARCHAR(255),
                                   price_per_person DECIMAL(19, 2),
                                   duration_type VARCHAR(20),
                                   duration_value INT,

    -- Dữ liệu gộp để phục vụ AI (Highlights, Mô tả...)
                                   highlights_text TEXT,

    -- Trí mạng của hệ thống: Vector Embedding
                                   embedding VECTOR(1536)
);

CREATE INDEX idx_tour_search_category ON tour_search_index(category_name);
CREATE INDEX idx_tour_search_price ON tour_search_index(price_per_person);
CREATE INDEX idx_tour_search_vector ON tour_search_index USING hnsw (embedding vector_cosine_ops);