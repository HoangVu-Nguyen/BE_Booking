-- 1. Thêm cột status (Lưu dưới dạng chuỗi VARCHAR để dễ map với Enum MediaStatus)
-- Set mặc định là 'PENDING' cho an toàn
ALTER TABLE message_attachments
    ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';

-- 2. Gỡ bỏ ràng buộc NOT NULL của cột message_id
-- Để cho phép lưu file nháp lên S3 trước khi user bấm gửi tin nhắn
ALTER TABLE message_attachments
    ALTER COLUMN message_id DROP NOT NULL;