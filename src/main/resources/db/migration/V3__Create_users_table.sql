-- Tạo bảng users
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255),
                       phone_number VARCHAR(20),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255),
                       bio TEXT,
                       is_active BOOLEAN NOT NULL DEFAULT FALSE,
                       birth_date DATE,
                       gender VARCHAR(20),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Khóa ngoại trỏ sang bảng ringtones
                       ringtone_id BIGINT,
                       CONSTRAINT fk_users_ringtone FOREIGN KEY (ringtone_id) REFERENCES ringtones(id)
);

-- Tạo bảng trung gian user_roles
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id INT NOT NULL,

    -- Khóa chính kép
                            PRIMARY KEY (user_id, role_id),

    -- Khóa ngoại có thêm ON DELETE CASCADE để tự động xóa quyền nếu user bị xóa
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);