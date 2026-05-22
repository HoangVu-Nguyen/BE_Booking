ALTER TABLE homestay_rooms
    ADD COLUMN is_instant_book BOOLEAN DEFAULT TRUE;

-- Cập nhật comment cho cột mới để quản trị viên dễ hiểu (tùy chọn)
COMMENT ON COLUMN homestay_rooms.is_instant_book IS 'Cho phép đặt phòng tức thì mà không cần Host duyệt';