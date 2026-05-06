package clyvasync.Clyvasync.service.homestay;



import clyvasync.Clyvasync.dto.request.HomestayRequest;
import clyvasync.Clyvasync.dto.response.HomestayDetailResponse;
import clyvasync.Clyvasync.dto.response.HomestayResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
public interface HomestayService {

    HomestayResponse createHomestay(HomestayRequest request, Long ownerId);

    HomestayResponse updateHomestay(Long id, HomestayRequest request, Long ownerId);

    void deleteHomestay(Long id, Long ownerId);

    HomestayResponse getById(Long id);


    Page<HomestayResponse> searchHomestays(
            String city,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer guests,
            Double minRating,
            Pageable pageable
    );

    List<HomestayResponse> getByOwnerId(Long ownerId);


    void updateStatus(Long id, String status, Long ownerId);

    void updateAverageRating(Long id, BigDecimal newRating);
    HomestayDetailResponse getHomestayDetail(Long id);
}