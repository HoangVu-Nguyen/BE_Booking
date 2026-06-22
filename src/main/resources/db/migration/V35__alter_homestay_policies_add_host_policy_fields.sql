
ALTER TABLE homestay_policies
    ADD COLUMN IF NOT EXISTS check_in_until TIME DEFAULT '22:00:00',
    ADD COLUMN IF NOT EXISTS min_nights INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS max_nights INT,
    ADD COLUMN IF NOT EXISTS booking_mode VARCHAR(30) DEFAULT 'INSTANT_BOOKING',
    ADD COLUMN IF NOT EXISTS cancellation_policy VARCHAR(30) DEFAULT 'FLEXIBLE',
    ADD COLUMN IF NOT EXISTS allows_children BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS quiet_hours_enabled BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS quiet_from TIME DEFAULT '22:00:00',
    ADD COLUMN IF NOT EXISTS quiet_to TIME DEFAULT '06:00:00',
    ADD COLUMN IF NOT EXISTS deposit_required BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deposit_amount DECIMAL(19, 2),
    ADD COLUMN IF NOT EXISTS extra_notes TEXT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Set default cho các cột cũ
ALTER TABLE homestay_policies
    ALTER COLUMN check_in_time SET DEFAULT '14:00:00',
ALTER COLUMN check_out_time SET DEFAULT '12:00:00',
    ALTER COLUMN allows_pets SET DEFAULT FALSE,
    ALTER COLUMN allows_smoking SET DEFAULT FALSE,
    ALTER COLUMN allows_parties SET DEFAULT FALSE;

-- Backfill dữ liệu cũ nếu đang NULL
UPDATE homestay_policies
SET
    check_in_time = COALESCE(check_in_time, '14:00:00'),
    check_in_until = COALESCE(check_in_until, '22:00:00'),
    check_out_time = COALESCE(check_out_time, '12:00:00'),
    min_nights = COALESCE(min_nights, 1),
    booking_mode = COALESCE(booking_mode, 'INSTANT_BOOKING'),
    cancellation_policy = COALESCE(cancellation_policy, 'FLEXIBLE'),
    allows_children = COALESCE(allows_children, TRUE),
    allows_pets = COALESCE(allows_pets, FALSE),
    allows_smoking = COALESCE(allows_smoking, FALSE),
    allows_parties = COALESCE(allows_parties, FALSE),
    quiet_hours_enabled = COALESCE(quiet_hours_enabled, TRUE),
    quiet_from = COALESCE(quiet_from, '22:00:00'),
    quiet_to = COALESCE(quiet_to, '06:00:00'),
    deposit_required = COALESCE(deposit_required, FALSE),
    created_at = COALESCE(created_at, CURRENT_TIMESTAMP),
    updated_at = COALESCE(updated_at, CURRENT_TIMESTAMP);

-- Set NOT NULL cho các field entity đang nullable = false
ALTER TABLE homestay_policies
    ALTER COLUMN check_in_time SET NOT NULL,
ALTER COLUMN check_out_time SET NOT NULL,
    ALTER COLUMN min_nights SET NOT NULL,
    ALTER COLUMN booking_mode SET NOT NULL,
    ALTER COLUMN cancellation_policy SET NOT NULL,
    ALTER COLUMN allows_children SET NOT NULL,
    ALTER COLUMN allows_pets SET NOT NULL,
    ALTER COLUMN allows_smoking SET NOT NULL,
    ALTER COLUMN allows_parties SET NOT NULL,
    ALTER COLUMN quiet_hours_enabled SET NOT NULL,
    ALTER COLUMN deposit_required SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;