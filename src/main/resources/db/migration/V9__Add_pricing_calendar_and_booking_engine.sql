CREATE TABLE homestay_price_calendar (
                                         id BIGSERIAL PRIMARY KEY,
                                         homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
                                         calendar_date DATE NOT NULL,
                                         price DECIMAL(19, 2) NOT NULL,
                                         is_available BOOLEAN DEFAULT TRUE,
                                         CONSTRAINT uk_homestay_date UNIQUE (homestay_id, calendar_date)
);

CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          booking_code VARCHAR(20) NOT NULL UNIQUE,
                          homestay_id BIGINT NOT NULL REFERENCES homestays(id),
                          user_id BIGINT NOT NULL REFERENCES users(id),
                          check_in_date DATE NOT NULL,
                          check_out_date DATE NOT NULL,
                          guest_count INT NOT NULL DEFAULT 1,
                          total_price DECIMAL(19, 2) NOT NULL,
                          status VARCHAR(50) DEFAULT 'PENDING',
                          payment_status VARCHAR(50) DEFAULT 'UNPAID',
                          cancellation_reason TEXT,
                          version INT DEFAULT 0 NOT NULL,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT check_dates CHECK (check_out_date > check_in_date)
);