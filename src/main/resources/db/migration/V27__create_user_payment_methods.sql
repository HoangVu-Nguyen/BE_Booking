-- =======================================================================
-- BẢNG PHƯƠNG THỨC THANH TOÁN / THẺ TÍN DỤNG (Chỉ lưu Metadata & Token hóa)
-- =======================================================================
CREATE TABLE user_payment_methods (
                                      id BIGSERIAL PRIMARY KEY,
                                      user_id BIGINT NOT NULL,                        -- Đồng bộ style lưu ID thô của hệ thống ông

                                      provider VARCHAR(50) NOT NULL DEFAULT 'STRIPE', -- Tên cổng thanh toán (STRIPE, PAYPAL, PAYOS, v.v.)
                                      gateway_token TEXT NOT NULL,                     -- Mã Token an toàn dùng để charge tiền từ xa thay thế số thẻ thật

                                      card_brand VARCHAR(30) NOT NULL,                -- 'VISA', 'MASTERCARD', 'AMERICAN_EXPRESS'
                                      card_type VARCHAR(20) DEFAULT 'CREDIT',         -- 'CREDIT' (Tín dụng) hoặc 'DEBIT' (Ghi nợ)
                                      last_four VARCHAR(4) NOT NULL,                  -- Chỉ lưu 4 số cuối (Ví dụ: '8842') để render lên UI

                                      exp_month INT NOT NULL,                         -- Tháng hết hạn (Ví dụ: 9)
                                      exp_year INT NOT NULL,                          -- Năm hết hạn (Ví dụ: 2026)
                                      card_holder_name VARCHAR(255) NOT NULL,         -- Tên in trên thẻ (Ví dụ: 'Julian R. Vane')

                                      is_primary BOOLEAN DEFAULT FALSE,               -- Thẻ mặc định (Primary) của User đó
                                      status VARCHAR(50) DEFAULT 'ACTIVE',            -- 'ACTIVE', 'EXPIRED', 'LOCKED'

                                      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Đánh chỉ mục (Index) để khi User vào trang Wallet, hệ thống lôi danh sách thẻ ra siêu tốc
CREATE INDEX idx_upm_user_id ON user_payment_methods(user_id);

-- Ràng buộc: Mỗi User chỉ có duy nhất 1 thẻ được Set làm Primary (True) tại 1 thời điểm công nghệ
CREATE UNIQUE INDEX uk_upm_user_primary ON user_payment_methods (user_id) WHERE (is_primary = TRUE);