ALTER TABLE bookings ADD COLUMN user_voucher_id BIGINT;
ALTER TABLE bookings ADD COLUMN discount_amount DECIMAL(19, 2) DEFAULT 0.00;
ALTER TABLE bookings ADD COLUMN platform_discount_amount DECIMAL(19, 2) DEFAULT 0.00;
ALTER TABLE bookings ADD COLUMN host_discount_amount DECIMAL(19, 2) DEFAULT 0.00;
