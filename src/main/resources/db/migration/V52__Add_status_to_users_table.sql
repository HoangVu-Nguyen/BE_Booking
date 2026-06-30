ALTER TABLE users ADD COLUMN status VARCHAR(50);


UPDATE users
SET status = CASE
                 WHEN is_active = true THEN 'ACTIVE'
                 ELSE 'INACTIVE'
    END;

ALTER TABLE users ALTER COLUMN status SET NOT NULL;
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'INACTIVE';
ALTER TABLE users ADD COLUMN suspended_until TIMESTAMP WITH TIME ZONE;