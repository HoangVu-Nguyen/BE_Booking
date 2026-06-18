package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayAmenity;
import org.mapstruct.Mapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomestayAmenityRepository extends JpaRepository<HomestayAmenity, Long> {
    List<HomestayAmenity> findByHomestayId(Long homestayId);

    void deleteByHomestayId(Long homestayId);
}
