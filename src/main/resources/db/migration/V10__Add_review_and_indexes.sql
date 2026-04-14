CREATE TABLE reviews (
                         id BIGSERIAL PRIMARY KEY,
                         booking_id BIGINT NOT NULL UNIQUE REFERENCES bookings(id),
                         homestay_id BIGINT NOT NULL REFERENCES homestays(id),
                         user_id BIGINT NOT NULL REFERENCES users(id),
                         rating INT CHECK (rating >= 1 AND rating <= 5),
                         comment TEXT,
                         created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_homestays_search ON homestays(city, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_calendar_search ON homestay_price_calendar(homestay_id, calendar_date, is_available);
CREATE INDEX idx_bookings_code ON bookings(booking_code);