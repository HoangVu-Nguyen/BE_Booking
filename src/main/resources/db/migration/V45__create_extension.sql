CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_amenity_alias_trgm ON amenity_aliases USING GIN (normalized_alias gin_trgm_ops);