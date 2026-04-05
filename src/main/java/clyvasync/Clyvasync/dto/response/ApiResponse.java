package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.exception.ResultCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int code;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();


    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(1000)
                .message("Successful")
                .data(data)
                .build();
    }


    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .code(1000)
                .message("Successful")
                .build();
    }


    public static <T> ApiResponse<T> error(ResultCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }


    public static <T> ApiResponse<T> error(ResultCode errorCode, String customMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(customMessage)
                .build();
    }
    public static <T> ApiResponse<T> success(ResultCode resultCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(resultCode.getCode())
                .message(resultCode.getMessage())
                .data(null)
                .build();
    }
}