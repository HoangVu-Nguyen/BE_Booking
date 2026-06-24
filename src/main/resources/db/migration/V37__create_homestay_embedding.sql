CREATE TABLE homestay_embedding (
                                    id SERIAL PRIMARY KEY,
                                    homestay_id BIGINT UNIQUE REFERENCES homestays(id),
                                    embedding VECTOR(1536)
);

CREATE INDEX ON homestay_embedding USING hnsw (embedding vector_cosine_ops);