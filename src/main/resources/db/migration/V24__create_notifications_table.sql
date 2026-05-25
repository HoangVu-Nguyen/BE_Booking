-- =========================================================================
-- V24: Khởi tạo bảng notifications (Thông báo đa hình)
-- =========================================================================

CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               recipient_id BIGINT NOT NULL,
                               type VARCHAR(50) NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,

                               metadata JSONB,

                               is_read BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);



CREATE INDEX idx_notifications_recipient_id
    ON notifications(recipient_id);

CREATE INDEX idx_notifications_recipient_unread
    ON notifications(recipient_id, is_read)
    WHERE is_read = FALSE;


CREATE INDEX idx_notifications_metadata
    ON notifications USING GIN (metadata);