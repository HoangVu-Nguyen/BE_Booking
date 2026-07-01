CREATE TABLE host_voucher_apply_scope (
    voucher_id BIGINT NOT NULL REFERENCES voucher_templates(id) ON DELETE CASCADE,
    homestay_id BIGINT NOT NULL REFERENCES homestays(id) ON DELETE CASCADE,
    PRIMARY KEY (voucher_id, homestay_id)
);
