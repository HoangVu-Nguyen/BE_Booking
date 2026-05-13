CREATE INDEX idx_homestays_location_id ON homestays(location_id);

CREATE INDEX idx_homestays_price ON homestays(base_price);

CREATE INDEX idx_calendar_availability ON homestay_calendar(night_date, is_available);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);

CREATE INDEX idx_homestays_avg_rating ON homestays(average_rating DESC);