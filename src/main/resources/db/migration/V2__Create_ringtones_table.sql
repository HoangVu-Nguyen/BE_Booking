-- Tạo bảng ringtones
CREATE TABLE ringtones (
                           id BIGSERIAL PRIMARY KEY,
                           ringtone_type VARCHAR(50) NOT NULL,
                           url VARCHAR(500) NOT NULL
);