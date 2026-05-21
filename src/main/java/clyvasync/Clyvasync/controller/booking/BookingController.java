package clyvasync.Clyvasync.controller.booking;

import clyvasync.Clyvasync.dto.request.BookingInitRequest;
import clyvasync.Clyvasync.dto.request.UpdateBookingContactRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.BookingDetailsResponse;
import clyvasync.Clyvasync.dto.response.BookingInitResponse;
import clyvasync.Clyvasync.dto.response.HostBookingItemResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.booking.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/bookings")
@AllArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/homestays/{homestayId}/unavailable-dates")
    public ApiResponse<List<LocalDate>> getUnavailableDates(
            @PathVariable Long homestayId,
            @RequestParam int month,
            @RequestParam int year) {

        List<LocalDate> blockedDates = bookingService.getUnavailableDates(homestayId, month, year);

        return ApiResponse.success(blockedDates);
    }
    @PostMapping("/init")
    public ApiResponse<BookingInitResponse> initBooking(
            @RequestBody BookingInitRequest request,
            @CurrentUserId Long userId) {

        BookingInitResponse response = bookingService.initBooking(request, userId);

        return ApiResponse.success(response);
    }

    @GetMapping("/{bookingCode}")
    public ApiResponse<BookingDetailsResponse> getBookingDetails(@PathVariable String bookingCode) {

        BookingDetailsResponse response = bookingService.getBookingDetailsByCode(bookingCode);

        return ApiResponse.success(response);
    }
    @GetMapping("/host/list")
    public ApiResponse<List<HostBookingItemResponse>> getHostBookings(
            @CurrentUserId Long ownerId
    ) {
        return ApiResponse.success( bookingService.getHostBookings(ownerId));
    }
    @PutMapping("/{bookingCode}/contact")
    public ApiResponse<Void> updateContactInfo(
            @PathVariable String bookingCode,
            @RequestBody UpdateBookingContactRequest request) {

        bookingService.updateContactInfo(bookingCode, request);
        return ApiResponse.success();
    }

}
