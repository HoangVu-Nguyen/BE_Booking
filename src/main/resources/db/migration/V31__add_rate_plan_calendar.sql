-- Bước 1: Xóa cột price_override ở bảng lịch phòng (Vì giá không đi theo Phòng)
ALTER TABLE room_calendar DROP COLUMN price_override;

-- Bước 2: Tạo bảng Lịch Giá riêng (Đi theo từng Gói giá)
CREATE TABLE rate_plan_calendar (
                                    id BIGSERIAL PRIMARY KEY,

                                    rate_plan_id BIGINT NOT NULL
                                        REFERENCES room_rate_plans(id)
                                            ON DELETE CASCADE,

                                    night_date DATE NOT NULL,

                                    price DECIMAL(19, 2) NOT NULL,

                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT uk_rate_plan_calendar_rate_plan_night
                                        UNIQUE (rate_plan_id, night_date)
);

CREATE INDEX idx_rate_plan_calendar_rate_plan_id
    ON rate_plan_calendar(rate_plan_id);

CREATE INDEX idx_rate_plan_calendar_night_date
    ON rate_plan_calendar(night_date);

CREATE INDEX idx_rate_plan_calendar_rate_plan_date
    ON rate_plan_calendar(rate_plan_id, night_date);