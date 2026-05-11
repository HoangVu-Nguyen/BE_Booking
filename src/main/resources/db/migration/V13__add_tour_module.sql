
CREATE TABLE tours (
                       id BIGSERIAL PRIMARY KEY,
                       homestay_id BIGINT NOT NULL REFERENCES homestays(id),
                       name VARCHAR(255) NOT NULL,
                       description TEXT,

                       duration_type VARCHAR(50) NOT NULL, -- Ví dụ: 'HOURS', 'HALF_DAY', 'FULL_DAY'
                       duration_value INT NOT NULL,

                       price_per_person DECIMAL(19, 2) NOT NULL,
                       max_participants INT NOT NULL DEFAULT 1,

                       allow_external_guests BOOLEAN DEFAULT FALSE,
                       status VARCHAR(50) DEFAULT 'ACTIVE',

                       version INT DEFAULT 0 NOT NULL,
                       deleted_at TIMESTAMP WITH TIME ZONE,
                       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT chk_tour_price CHECK (price_per_person >= 0),
                       CONSTRAINT chk_tour_participants CHECK (max_participants > 0)
);

CREATE INDEX idx_tours_homestay ON tours(homestay_id) WHERE deleted_at IS NULL;

CREATE TABLE tour_images (
                             id BIGSERIAL PRIMARY KEY,
                             tour_id BIGINT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
                             image_url TEXT NOT NULL,
                             is_primary BOOLEAN DEFAULT FALSE,
                             display_order INT DEFAULT 0,
                             created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tour_images_tour ON tour_images(tour_id);

CREATE TABLE tour_bookings (
                               id BIGSERIAL PRIMARY KEY,
                               booking_code VARCHAR(20) NOT NULL UNIQUE,
                               tour_id BIGINT NOT NULL REFERENCES tours(id),
                               user_id BIGINT NOT NULL REFERENCES users(id),

                               homestay_booking_id BIGINT REFERENCES bookings(id) ON DELETE RESTRICT,

                               tour_date DATE NOT NULL,
                               participant_count INT NOT NULL DEFAULT 1,
                               total_price DECIMAL(19, 2) NOT NULL,

                               status VARCHAR(50) DEFAULT 'PENDING',
                               payment_status VARCHAR(50) DEFAULT 'UNPAID',

                               cancellation_reason TEXT,
                               version INT DEFAULT 0 NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_tour_booking_participants CHECK (participant_count > 0)
);

CREATE INDEX idx_tour_bookings_user ON tour_bookings(user_id);
CREATE INDEX idx_tour_bookings_homestay_booking ON tour_bookings(homestay_booking_id);