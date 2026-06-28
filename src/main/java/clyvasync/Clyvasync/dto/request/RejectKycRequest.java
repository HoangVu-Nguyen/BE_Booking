package clyvasync.Clyvasync.dto.request;

import lombok.Data;

@Data
public class RejectKycRequest {
    private String reason; // VD: "Ảnh CCCD bị chói sáng, vui lòng chụp lại"
}