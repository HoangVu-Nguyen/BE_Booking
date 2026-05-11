package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BookTourRequest(
        @NotNull(message = "ID Tour không được để trống")
        Long tourId,

        @NotNull(message = "Ngày đi tour không được để trống")
        @FutureOrPresent(message = "Ngày đi tour không được ở trong quá khứ")
        LocalDate tourDate,

        @NotNull(message = "Số lượng người không được để trống")
        @Min(value = 1, message = "Phải có ít nhất 1 người tham gia")
        Integer participantCount

        // Lưu ý: userId không lấy từ Request Body mà nên lấy từ Security Context (Token)
        // homestayBookingId cũng không nhận từ user nhập, mà do hệ thống tự mapping
        // nếu họ book tour trong luồng book phòng.
) {}
