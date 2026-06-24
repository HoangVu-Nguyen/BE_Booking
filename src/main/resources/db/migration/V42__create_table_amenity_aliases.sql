CREATE TABLE IF NOT EXISTS amenity_aliases (
                                               id BIGSERIAL PRIMARY KEY,
                                               amenity_id INTEGER NOT NULL REFERENCES amenities(id) ON DELETE CASCADE,
    alias TEXT NOT NULL,
    normalized_alias TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_amenity_alias UNIQUE (amenity_id, normalized_alias)
    );

CREATE INDEX IF NOT EXISTS idx_amenity_aliases_normalized
    ON amenity_aliases (normalized_alias);