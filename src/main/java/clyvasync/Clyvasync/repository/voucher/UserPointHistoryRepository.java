package clyvasync.Clyvasync.repository.voucher;

import clyvasync.Clyvasync.modules.voucher.entity.UserPointHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPointHistoryRepository extends JpaRepository<UserPointHistory, Long> {
    
    @Query("SELECT COALESCE(SUM(p.points), 0) FROM UserPointHistory p WHERE p.userId = :userId")
    Integer sumPointsByUserId(@Param("userId") Long userId);
}
