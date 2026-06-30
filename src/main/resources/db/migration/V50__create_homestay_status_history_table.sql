CREATE TABLE homestay_status_history (
                                         id BIGSERIAL PRIMARY KEY,
                                         homestay_id BIGINT NOT NULL,
                                         old_status VARCHAR(50),
                                         new_status VARCHAR(50),
                                         changed_by VARCHAR(255),
                                         reason TEXT,
                                         created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                         CONSTRAINT fk_homestay_history
                                             FOREIGN KEY (homestay_id)
                                                 REFERENCES homestays(id)
                                                 ON DELETE CASCADE
);

CREATE INDEX idx_homestay_status_history_id ON homestay_status_history(homestay_id);