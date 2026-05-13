package clyvasync.Clyvasync.controller.booking;

import clyvasync.Clyvasync.dto.response.ApiResponse;
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

}
