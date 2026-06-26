

UPDATE homestay_search_index SET embedding = NULL;
ALTER TABLE homestay_search_index ALTER COLUMN embedding TYPE vector(768);