package clyvasync.Clyvasync.dto.request;

import lombok.Data;

@Data
public class StatusUpdateRequest {
    private String status; // Ví dụ: "SUSPENDED" hoặc "APPROVED"
    private String reason; // Lý do khóa hoặc mở lại
}