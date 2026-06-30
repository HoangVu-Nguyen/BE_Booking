CREATE TABLE host_audit_logs (
                                 id BIGSERIAL PRIMARY KEY,
                                 host_id BIGINT NOT NULL,
                                 action VARCHAR(50) NOT NULL,
                                 reason TEXT NOT NULL,
                                 created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_host_audit_logs_host_id ON host_audit_logs(host_id);