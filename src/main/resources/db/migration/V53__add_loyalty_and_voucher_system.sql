
CREATE TABLE voucher_templates (
                                   id BIGSERIAL PRIMARY KEY,
                                   code VARCHAR(50) UNIQUE,
                                   name VARCHAR(255) NOT NULL,
                                   description TEXT,
                                   discount_type VARCHAR(20) NOT NULL, -- 'FIXED_AMOUNT', 'PERCENTAGE'
                                   discount_value DECIMAL(19, 2) NOT NULL,
                                   max_discount DECIMAL(19, 2),
                                   min_order_value DECIMAL(19, 2) DEFAULT 0,
                                   points_required INT NOT NULL DEFAULT 0,
                                   sponsor_type VARCHAR(20) DEFAULT 'PLATFORM', -- 'PLATFORM', 'HOST'
                                   valid_from TIMESTAMP WITH TIME ZONE,
                                   valid_until TIMESTAMP WITH TIME ZONE,


                                   total_issue_limit INT, -- Giới hạn số lượng cho phép đổi/lưu (NULL là vô hạn)
                                   current_issue_count INT DEFAULT 0, -- Số lượng thực tế đã bị đổi/lưu

                                   total_usage_limit INT, -- Giới hạn số lượng cho phép thanh toán thành công
                                   current_usage_count INT DEFAULT 0, -- Số lượng thực tế đã thanh toán thành công

                                   version INT DEFAULT 0 NOT NULL, -- Bắt buộc cho JPA @Version (Chống Race Condition)
    -- ==========================================

                                   is_active BOOLEAN DEFAULT TRUE,
                                   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE user_vouchers (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT NOT NULL,
                               template_id BIGINT NOT NULL,
                               status VARCHAR(20) DEFAULT 'AVAILABLE', -- 'AVAILABLE', 'USED', 'EXPIRED'
                               used_at TIMESTAMP WITH TIME ZONE,
                               used_on_booking_id BIGINT,
                               created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_vouchers_user_id ON user_vouchers(user_id);
CREATE INDEX idx_user_vouchers_status ON user_vouchers(status);


CREATE TABLE user_point_history (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT NOT NULL,
                                    transaction_type VARCHAR(20) NOT NULL, -- 'EARN', 'REDEEM', 'REFUND', 'EXPIRED'
                                    points INT NOT NULL,
                                    reference_id BIGINT, -- ID của booking hoặc voucher liên quan
                                    description TEXT,
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_point_history_user_id ON user_point_history(user_id);


ALTER TABLE users ADD COLUMN total_points INT DEFAULT 0;

ALTER TABLE bookings
    ADD COLUMN platform_voucher_id BIGINT,
ADD COLUMN platform_discount DECIMAL(19,2) DEFAULT 0,
ADD COLUMN host_voucher_id BIGINT,
ADD COLUMN host_discount DECIMAL(19,2) DEFAULT 0;