-- Quản lý giá biến động và lịch trống
CREATE TABLE homestay_calendar (
                                   id BIGSERIAL PRIMARY KEY,
                                   homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
                                   night_date DATE NOT NULL,
                                   price_override DECIMAL(19, 2), -- Nếu có giá đặc biệt cho ngày này
                                   is_available BOOLEAN DEFAULT TRUE,
                                   CONSTRAINT uk_homestay_night UNIQUE (homestay_id, night_date)
);

-- Quản lý đặt phòng
CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          booking_code VARCHAR(20) NOT NULL UNIQUE,
                          homestay_id BIGINT NOT NULL REFERENCES homestays(id),
                          user_id BIGINT NOT NULL, -- Guest ID
                          check_in_date DATE NOT NULL,
                          check_out_date DATE NOT NULL,
                          guest_count INT NOT NULL DEFAULT 1,
                          total_price DECIMAL(19, 2) NOT NULL,
                          tax_fee DECIMAL(19, 2) DEFAULT 0,
                          status VARCHAR(50) DEFAULT 'PENDING',
                          payment_status VARCHAR(50) DEFAULT 'UNPAID',
                          special_requests TEXT,
                          cancellation_reason TEXT,
                          version INT DEFAULT 0 NOT NULL, -- Optimistic Locking cho Booking
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT check_dates CHECK (check_out_date > check_in_date)
);

-- Đánh giá sau khi ở
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