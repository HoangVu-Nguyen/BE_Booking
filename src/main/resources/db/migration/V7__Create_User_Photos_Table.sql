-- 1. Tạo bảng user_photos
CREATE TABLE user_photos (
                             id BIGSERIAL PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             photo_url TEXT NOT NULL,
                             photo_type VARCHAR(20) NOT NULL, -- 'AVATAR' hoặc 'COVER'
                             is_current BOOLEAN DEFAULT FALSE NOT NULL,
                             created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

                             CONSTRAINT chk_photo_type CHECK (photo_type IN ('AVATAR', 'COVER'))
);


ALTER TABLE user_photos
    ADD CONSTRAINT fk_user_photos_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;


CREATE INDEX idx_user_photos_user_id ON user_photos(user_id);
CREATE INDEX idx_user_photos_current_type ON user_photos(user_id, photo_type) WHERE is_current = TRUE;

COMMENT ON TABLE user_photos IS 'Bảng lưu trữ lịch sử ảnh đại diện và ảnh bìa của người dùng';