-- 6. Đặt Tour
CREATE TABLE tour_bookings (
                               id BIGSERIAL PRIMARY KEY,
                               booking_code VARCHAR(20) NOT NULL UNIQUE,
                               tour_id BIGINT NOT NULL REFERENCES tours(id),
                               user_id BIGINT NOT NULL,
                               homestay_booking_id BIGINT REFERENCES bookings(id),
                               availability_id BIGINT NOT NULL REFERENCES tour_availability(id),

                               tour_date DATE NOT NULL, -- BỔ SUNG CỘT NÀY VÀO SQL CHO KHỚP VỚI JAVA

                               participant_count INT NOT NULL DEFAULT 1,
                               total_price DECIMAL(19, 2) NOT NULL,
                               status VARCHAR(50) DEFAULT 'PENDING',
                               payment_status VARCHAR(50) DEFAULT 'UNPAID',
                               special_requests TEXT,
                               cancellation_reason TEXT,
                               version INT DEFAULT 0 NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tour_participant_details (
                                          id BIGSERIAL PRIMARY KEY,
                                          tour_booking_id BIGINT NOT NULL REFERENCES tour_bookings(id) ON DELETE CASCADE,
                                          full_name VARCHAR(255),
                                          age INT,
                                          identity_number VARCHAR(20)
);

CREATE INDEX idx_tour_booking_code ON tour_bookings(booking_code);
CREATE INDEX idx_tour_booking_user ON tour_bookings(user_id);