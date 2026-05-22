-- Thêm cột is_approved vào bảng bookings
ALTER TABLE bookings
    ADD COLUMN is_approved BOOLEAN DEFAULT FALSE;

-- Cập nhật giá trị cho các bản ghi đã duyệt trước đó (nếu có)
-- Giả định rằng đơn hàng nào đã ở trạng thái CONFIRMED hoặc AWAITING_PAYMENT
-- thì chắc chắn đã được Host duyệt qua
UPDATE bookings
SET is_approved = TRUE
WHERE status IN ('CONFIRMED', 'AWAITING_PAYMENT');

-- Thêm comment để dễ quản lý trong DB (tùy chọn)
COMMENT ON COLUMN bookings.is_approved IS 'Đánh dấu đơn hàng đã từng được Host duyệt';