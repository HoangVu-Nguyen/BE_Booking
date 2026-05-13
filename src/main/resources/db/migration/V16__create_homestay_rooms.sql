CREATE TABLE homestay_rooms (
                                id BIGSERIAL PRIMARY KEY,
                                homestay_id BIGINT NOT NULL,
                                name VARCHAR(255) NOT NULL,
                                max_guests INT NOT NULL DEFAULT 1,
                                bed_count INT NOT NULL DEFAULT 1,
                                price DECIMAL(15, 2) NOT NULL,
                                quantity INT NOT NULL DEFAULT 1,
                                status VARCHAR(50) DEFAULT 'ACTIVE',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE room_amenity_mapping (
                                      id BIGSERIAL PRIMARY KEY,
                                      room_id BIGINT NOT NULL,
                                      amenity_id INT NOT NULL,
                                      CONSTRAINT uc_room_amenity UNIQUE (room_id, amenity_id)
);

CREATE INDEX idx_room_amenity_room_id ON room_amenity_mapping(room_id);