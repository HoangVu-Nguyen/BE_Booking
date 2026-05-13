-- 1. BẢNG HÓA ĐƠN TỔNG
CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          booking_code VARCHAR(20) NOT NULL UNIQUE,
                          user_id BIGINT NOT NULL,
                          homestay_id BIGINT NOT NULL REFERENCES homestays(id),
                          total_price DECIMAL(19, 2) NOT NULL,
                          tax_fee DECIMAL(19, 2) DEFAULT 0,
                          status VARCHAR(50) DEFAULT 'PENDING',
                          payment_status VARCHAR(50) DEFAULT 'UNPAID',
                          special_requests TEXT,
                          cancellation_reason TEXT,
                          version INT DEFAULT 0 NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. BẢNG CHI TIẾT ĐẶT PHÒNG
CREATE TABLE booking_details (
                                 id BIGSERIAL PRIMARY KEY,
                                 booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
                                 room_id BIGINT NOT NULL REFERENCES homestay_rooms(id),
                                 rate_plan_id BIGINT NOT NULL REFERENCES room_rate_plans(id),

                                 check_in_date DATE NOT NULL,
                                 check_out_date DATE NOT NULL,
                                 quantity INT NOT NULL DEFAULT 1,
                                 guest_count INT NOT NULL DEFAULT 1,

                                 unit_price DECIMAL(19, 2) NOT NULL,
                                 subtotal DECIMAL(19, 2) NOT NULL,

                                 CONSTRAINT check_dates CHECK (check_out_date > check_in_date)
);

-- 3. BẢNG ĐÁNH GIÁ (Reviews)
CREATE TABLE reviews (
                         id BIGSERIAL PRIMARY KEY,
                         booking_id BIGINT UNIQUE REFERENCES bookings(id),
                         homestay_id BIGINT REFERENCES homestays(id),
                         guest_id BIGINT NOT NULL,
                         rating INT CHECK (rating >= 1 AND rating <= 5),
                         comment TEXT,
                         reply_from_host TEXT,
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_homestay_id ON bookings(homestay_id);
CREATE INDEX idx_booking_details_booking_id ON booking_details(booking_id);