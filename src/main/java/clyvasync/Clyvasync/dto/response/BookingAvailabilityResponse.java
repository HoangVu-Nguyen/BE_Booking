package clyvasync.Clyvasync.dto.response;


import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BookingAvailabilityResponse {
    private List<RoomResponse> rooms;

    private List<TourResponse> suggestedTours;


}