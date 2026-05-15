package clyvasync.Clyvasync.service.tour;

import clyvasync.Clyvasync.dto.request.CreateTourRequest;
import clyvasync.Clyvasync.dto.request.UpdateTourRequest;
import clyvasync.Clyvasync.dto.response.TourDetailResponse;
import clyvasync.Clyvasync.dto.response.TourResponse;
import clyvasync.Clyvasync.enums.type.TourStatus;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TourService {

    TourResponse createTour(Long homestayId, CreateTourRequest request);

    TourResponse updateTour(Long tourId, UpdateTourRequest request);

    void deleteTour(Long tourId);

    void updateTourStatus(Long tourId, TourStatus status);


    TourDetailResponse getTourById(Long tourId);

    List<TourResponse> getToursByHomestayId(Long homestayId);


  List<TourResponse> getExternalToursByHomestayId(Long homestayId);

    Page<TourResponse> searchTours(String query, Long homestayId, TourStatus status, Pageable pageable);
    Page<TourResponse> getAllTours(Pageable pageable);
    List<TourResponse> getAvailableToursForBookingDates(Long homestayId, LocalDate checkIn, LocalDate checkOut);
    Tour findTourById(Long tourId);
    List<Tour> findAllByIds(List<Long> tourIds);
}
