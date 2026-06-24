ALTER TABLE homestay_search_index
    ADD COLUMN amenity_codes text[] DEFAULT '{}';

CREATE INDEX idx_homestay_search_amenity_codes
    ON homestay_search_index USING GIN (amenity_codes);