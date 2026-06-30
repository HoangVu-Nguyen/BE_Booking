package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomestayStatusHistoryRepository extends JpaRepository<HomestayStatusHistory,Long> {
    List<HomestayStatusHistory> findByHomestayIdOrderByCreatedAtDesc(Long homestayId);
}
