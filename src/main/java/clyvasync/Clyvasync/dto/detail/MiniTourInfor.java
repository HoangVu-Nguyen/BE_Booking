package clyvasync.Clyvasync.dto.detail;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor

public class MiniTourInfor {

        private String name;
        private String image;
        private BigDecimal pricePerPerson;
        private int count;
        private LocalDate startDate;
        private LocalTime startTime;

}
