-- Tạo bảng ringtones
CREATE TABLE ringtones (
                           id BIGSERIAL PRIMARY KEY,
                           ringtone_type VARCHAR(50) NOT NULL,
                           url VARCHAR(500) NOT NULL
);
INSERT INTO ringtones (ringtone_type, url)
VALUES
    ('RINGTONE', 'https://assets.clyvasync.com/sounds/default-ringtone.mp3'),
    ('NOTIFICATION', 'https://assets.clyvasync.com/sounds/default-notification.mp3'),
    ('MESSAGE', 'https://assets.clyvasync.com/sounds/default-message.mp3'),
    ('CALL', 'https://assets.clyvasync.com/sounds/default-call.mp3');