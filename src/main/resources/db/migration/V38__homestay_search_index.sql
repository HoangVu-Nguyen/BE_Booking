
CREATE TABLE homestay_search_index (
                                       room_id BIGINT PRIMARY KEY REFERENCES homestay_rooms(id) ON DELETE CASCADE,
                                       homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
                                       name VARCHAR(255),
                                       city VARCHAR(100),
                                       max_guests INT,
                                       bed_count INT,
                                       price_current DECIMAL(19, 2),
                                       average_rating DECIMAL(3,2) DEFAULT 0.00,
                                       review_count INT DEFAULT 0,
                                       amenities_tsv TSVECTOR,
                                       embedding VECTOR(1536)
);
CREATE INDEX idx_search_city ON homestay_search_index(city);
CREATE INDEX idx_search_guests ON homestay_search_index(max_guests);
CREATE INDEX idx_search_amenities ON homestay_search_index USING GIN(amenities_tsv);
CREATE INDEX idx_search_vector ON homestay_search_index USING hnsw (embedding vector_cosine_ops);