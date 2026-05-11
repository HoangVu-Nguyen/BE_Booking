package clyvasync.Clyvasync.dto.request;


import clyvasync.Clyvasync.enums.type.DurationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTourRequest(
        @NotBlank(message = "Tên tour không được để trống")
        String name,

        String description,

        @NotNull(message = "Vui lòng chọn loại thời gian (Giờ/Ngày)")
        DurationType durationType,

        @NotNull(message = "Vui lòng nhập độ dài thời gian")
        @Min(value = 1, message = "Thời gian phải lớn hơn 0")
        Integer durationValue,

        @NotNull(message = "Vui lòng nhập giá")
        @Min(value = 0, message = "Giá không được âm")
        BigDecimal pricePerPerson,

        @NotNull(message = "Vui lòng nhập số người tối đa")
        @Min(value = 1, message = "Số người tối đa phải từ 1 trở lên")
        Integer maxParticipants,

        Boolean allowExternalGuests
) {}

