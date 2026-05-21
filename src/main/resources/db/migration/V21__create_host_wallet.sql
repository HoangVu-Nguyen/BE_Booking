-- BẢNG VÍ CHỦ NHÀ
CREATE TABLE host_wallets (
                              id BIGSERIAL PRIMARY KEY,
                              owner_id BIGINT NOT NULL UNIQUE, -- ID của user làm chủ nhà

                              pending_balance DECIMAL(19, 2) DEFAULT 0,   -- Tiền đang bị giam (Từ các booking sắp tới hoặc chưa qua 24h)
                              available_balance DECIMAL(19, 2) DEFAULT 0, -- Tiền có thể rút
                              total_withdrawn DECIMAL(19, 2) DEFAULT 0,   -- Tổng tiền đã rút từ trước đến nay

                              version INT DEFAULT 0 NOT NULL, -- Dùng cho Optimistic Locking khi cộng/trừ tiền
                              updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_host_wallets_owner_id ON host_wallets(owner_id);

-- BẢNG LỊCH SỬ GIAO DỊCH (Biến động số dư)
CREATE TABLE wallet_transactions (
                                     id BIGSERIAL PRIMARY KEY,
                                     wallet_id BIGINT NOT NULL REFERENCES host_wallets(id),
                                     booking_id BIGINT, -- Có thể NULL nếu là giao dịch rút tiền

                                     amount DECIMAL(19, 2) NOT NULL,
                                     transaction_type VARCHAR(50) NOT NULL, -- 'BOOKING_REVENUE', 'WITHDRAWAL', 'REFUND_DEDUCTION'
                                     status VARCHAR(50) NOT NULL,           -- 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'

                                     bank_account_info TEXT, -- Lưu "Vietcombank - 123456789 - NGUYEN VAN A" khi Host rút tiền
                                     description TEXT,

                                     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);