package clyvasync.Clyvasync.repository.host;

import clyvasync.Clyvasync.dto.projection.ActivityProjection;
import clyvasync.Clyvasync.modules.host.entity.HostAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HostAuditLogRepository extends JpaRepository<HostAuditLog, Long> {
    @Query(value = """
    SELECT * FROM (
        -- 1. LUỒNG ĐÌNH CHỈ / CẢNH CÁO HOST (Từ host_audit_logs)
        SELECT 
            'AUD-' || id AS id,
            'Đình chỉ Host ID ' || host_id || ': ' || action AS title,
            created_at AS time,
            'USER' AS type,
            'WARNING' AS status
        FROM host_audit_logs
        
        UNION ALL
        
        -- 2. LUỒNG KIỂM DUYỆT PHÒNG (Từ homestay_status_history)
        SELECT 
            'HST-' || id AS id,
            'Cập nhật phòng ID ' || homestay_id || ': ' || old_status || ' -> ' || new_status AS title,
            created_at AS time,
            'SYSTEM' AS type,
            CASE 
                WHEN new_status = 'APPROVED' THEN 'SUCCESS'
                WHEN new_status IN ('SUSPENDED', 'REJECTED') THEN 'WARNING'
                ELSE 'INFO' 
            END AS status
        FROM homestay_status_history
        
        UNION ALL
        
        -- 3. LUỒNG DUYỆT HỒ SƠ KYC (Từ host_kyc_profiles)
        SELECT 
            'KYC-' || id AS id,
            CASE 
                WHEN status = 'APPROVED' THEN 'Đã duyệt KYC: ' || legal_name
                WHEN status = 'REJECTED' THEN 'Từ chối KYC: ' || legal_name
                ELSE 'Yêu cầu KYC mới: ' || legal_name 
            END AS title,
            updated_at AS time,
            'USER' AS type,
            CASE 
                WHEN status = 'APPROVED' THEN 'SUCCESS'
                WHEN status = 'REJECTED' THEN 'WARNING'
                ELSE 'INFO' 
            END AS status
        FROM host_kyc_profiles
        
        UNION ALL
        
        -- 4. LUỒNG ĐẶT PHÒNG MỚI (Từ bookings)
        SELECT 
            'BKG-' || id AS id,
            'Booking: ' || booking_code || ' (' || total_price || ' VND)',
            created_at AS time,
            'BOOKING' AS type,
            CASE 
                WHEN status = 'COMPLETED' THEN 'SUCCESS'
                WHEN status IN ('CANCELLED', 'FAILED') THEN 'WARNING'
                ELSE 'INFO' 
            END AS status
        FROM bookings
        
        UNION ALL
        
        -- 5. LUỒNG ĐÁNH GIÁ TỪ KHÁCH HÀNG (Từ reviews)
        SELECT 
            'REV-' || id AS id,
            'Đánh giá mới (' || rating || ' sao) cho phòng ID ' || homestay_id AS title,
            created_at AS time,
            'BOOKING' AS type,
            CASE 
                WHEN rating >= 4 THEN 'SUCCESS'
                WHEN rating <= 2 THEN 'WARNING'
                ELSE 'INFO' 
            END AS status
        FROM reviews
        
        UNION ALL
        
        -- 6. LUỒNG RÚT TIỀN / THANH TOÁN (Giả định bảng wallet_transactions)
        -- Lưu ý: Bạn cần đổi tên bảng/cột này theo thực tế database của bạn
        SELECT 
            'FIN-' || id AS id,
            'Giao dịch ví: ' || transaction_type || ' (' || amount || ')',
            created_at AS time,
            'FINANCE' AS type,
            CASE 
                WHEN status = 'COMPLETED' THEN 'SUCCESS'
                WHEN status = 'FAILED' THEN 'WARNING'
                ELSE 'INFO' 
            END AS status
        FROM wallet_transactions
        
    ) AS combined_activities
    ORDER BY time DESC 
    LIMIT 15
    """, nativeQuery = true)
    List<ActivityProjection> getRecentActivities();
}
