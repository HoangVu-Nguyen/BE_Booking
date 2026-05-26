-- 1. Bảng Conversations (Phòng chat)
CREATE TABLE conversations (
                               id BIGSERIAL PRIMARY KEY,
                               type VARCHAR(50) NOT NULL, -- 'ADMIN', 'HOST', 'GROUP'
                               reference_id BIGINT, -- Lưu ID của Booking hoặc Tour
                               name VARCHAR(255), -- Tên nhóm (dành cho Group)
                               last_message_at TIMESTAMP(3) WITH TIME ZONE, -- Dùng để sort danh sách inbox
                               created_at TIMESTAMP(3) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng Conversation Participants (Thành viên trong phòng)
CREATE TABLE conversation_participants (
                                           id BIGSERIAL PRIMARY KEY,
                                           conversation_id BIGINT NOT NULL,
                                           user_id BIGINT NOT NULL,
                                           last_read_message_id BIGINT DEFAULT 0, -- Cực kỳ quan trọng để đếm tin chưa đọc
                                           joined_at TIMESTAMP(3) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                           CONSTRAINT uk_conversation_user UNIQUE (conversation_id, user_id)
);

-- Đánh Index để query danh sách phòng chat của 1 user cực nhanh
CREATE INDEX idx_cp_user_id ON conversation_participants(user_id);

-- 3. Bảng Messages (Lưu nội dung chữ)
CREATE TABLE messages (
                          id BIGSERIAL PRIMARY KEY,
                          conversation_id BIGINT NOT NULL,
                          sender_id BIGINT NOT NULL, -- ID = 0 nếu là System
                          content TEXT, -- Có thể null nếu tin nhắn chỉ có ảnh
                          type VARCHAR(50) NOT NULL, -- 'TEXT', 'IMAGE', 'SYSTEM'
                          created_at TIMESTAMP(3) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Bắt buộc phải có Index này để load lịch sử chat nhanh
CREATE INDEX idx_msg_conversation_created ON messages(conversation_id, created_at DESC);

-- 4. Bảng Message Attachments (Hỗ trợ Hình ảnh, File, Video)
CREATE TABLE message_attachments (
                                     id BIGSERIAL PRIMARY KEY,
                                     message_id BIGINT NOT NULL,
                                     file_url TEXT NOT NULL, -- Link ảnh từ S3/Cloudinary/Firebase
                                     file_type VARCHAR(50) NOT NULL, -- 'image/png', 'image/jpeg'...
                                     created_at TIMESTAMP(3) WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attachment_message_id ON message_attachments(message_id);