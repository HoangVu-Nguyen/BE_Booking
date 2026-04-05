-- 1. Tạo bảng Refresh Tokens
CREATE TABLE refresh_tokens (
                                id BIGSERIAL PRIMARY KEY,
                                token VARCHAR(500) NOT NULL UNIQUE,
                                email VARCHAR(255) NOT NULL,
                                expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
                                device_id VARCHAR(255) NOT NULL,
                                ip_address VARCHAR(45),
                                revoked BOOLEAN DEFAULT FALSE,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tạo bảng User Devices (Quản lý Session/Thiết bị)
CREATE TABLE user_devices (
                              id BIGSERIAL PRIMARY KEY,
                              user_id BIGINT NOT NULL,
    -- Thêm UNIQUE ở đây để đảm bảo quan hệ 1-1 với RefreshToken
                              refresh_token_id BIGINT UNIQUE,
                              device_name VARCHAR(255),
                              device_type VARCHAR(50),
                              ip_address VARCHAR(45),
                              location TEXT,
                              last_active TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Khóa ngoại liên kết với bảng refresh_tokens
                              CONSTRAINT fk_device_refresh_token
                                  FOREIGN KEY (refresh_token_id)
                                      REFERENCES refresh_tokens(id)
                                      ON DELETE CASCADE,

    -- Khóa ngoại liên kết với bảng users
                              CONSTRAINT fk_device_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);

-- 3. Đánh Index để tối ưu hiệu năng
-- Index tìm kiếm token (B-Tree)
CREATE INDEX idx_rt_token ON refresh_tokens (token);

-- Index tổ hợp cho việc dọn dẹp session cũ theo Email + Device (Thường dùng trong logic issueRefreshToken)
CREATE INDEX idx_rt_email_device ON refresh_tokens (email, device_id);

-- Index tìm kiếm nhanh các thiết bị của một User (Dùng cho trang quản lý thiết bị)
CREATE INDEX idx_ud_user_id ON user_devices (user_id);

-- 4. Trigger tự động cập nhật updated_at
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$BEGIN
    NEW.updated_at = now();
RETURN NEW;
END;$$ language 'plpgsql';

CREATE TRIGGER update_refresh_tokens_modtime
    BEFORE UPDATE ON refresh_tokens
    FOR EACH ROW
    EXECUTE PROCEDURE update_modified_column();