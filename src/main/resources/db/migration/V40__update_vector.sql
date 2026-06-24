-- 1. Sửa bảng Phòng (Homestay)
ALTER TABLE homestay_search_index DROP COLUMN embedding;
ALTER TABLE homestay_search_index ADD COLUMN embedding VECTOR(768);
CREATE INDEX idx_search_vector ON homestay_search_index USING hnsw (embedding vector_cosine_ops);

-- 2. Sửa bảng Tour
ALTER TABLE tour_search_index DROP COLUMN embedding;
ALTER TABLE tour_search_index ADD COLUMN embedding VECTOR(768);
CREATE INDEX idx_tour_search_vector ON tour_search_index USING hnsw (embedding vector_cosine_ops);