ALTER TABLE bookings
    -- Dòng tiền: Phân tách rõ tiền của Host và tiền phí của Nền tảng
    ADD COLUMN host_payout_amount DECIMAL(19,2) DEFAULT 0, -- Tiền Host sẽ nhận (VD: 90% total_price)
    ADD COLUMN platform_fee_amount DECIMAL(19,2) DEFAULT 0, -- Phí hoa hồng App giữ (VD: 10% total_price)

    -- Trạng thái giải ngân cho Host (Liên kết với PayoutStatus Enum)
    ADD COLUMN payout_status VARCHAR(50) DEFAULT 'NOT_APPLICABLE',

    -- Mốc thời gian thực tế để Cron Job làm việc
    ADD COLUMN actual_check_in_time TIMESTAMP WITH TIME ZONE,  -- Bắt đầu đếm ngược 24h từ lúc này
    ADD COLUMN actual_check_out_time TIMESTAMP WITH TIME ZONE,
    ADD COLUMN dispute_raised_at TIMESTAMP WITH TIME ZONE;     -- Thời điểm khách ấn nút "Khiếu nại"