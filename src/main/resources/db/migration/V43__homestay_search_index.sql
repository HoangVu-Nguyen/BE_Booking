ALTER TABLE homestay_search_index
    ADD COLUMN IF NOT EXISTS amenity_ids integer[] DEFAULT '{}';

CREATE INDEX IF NOT EXISTS idx_homestay_search_amenity_ids
    ON homestay_search_index USING GIN (amenity_ids);