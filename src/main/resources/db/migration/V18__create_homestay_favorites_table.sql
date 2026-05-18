CREATE TABLE homestay_favorites (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT NOT NULL,
                                    homestay_id BIGINT NOT NULL,
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT uk_user_homestay_favorite UNIQUE (user_id, homestay_id)
);

CREATE INDEX idx_homestay_favorites_user_id ON homestay_favorites(user_id);