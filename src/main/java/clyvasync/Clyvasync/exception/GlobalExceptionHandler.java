package clyvasync.Clyvasync.exception;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
        ResultCode errorCode = exception.getErrorCode();

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getBindingResult().getFieldError()).getDefaultMessage();

        ResultCode resultCode = ResultCode.INVALID_KEY;

        try {
            if (enumKey != null) {
                resultCode = ResultCode.valueOf(enumKey);
            }
        } catch (IllegalArgumentException e) {
            // Khi nhảy vào đây tức là bạn cấu hình message="XXX" ở DTO nhưng quên tạo Enum XXX.
            // Trạm xá sẽ tự động giữ nguyên resultCode = INVALID_KEY để trả về JSON một cách êm ái.
        }

        return ResponseEntity
                .status(resultCode.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .code(resultCode.getCode())
                        .message(resultCode.getMessage())
                        .build());
    }
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handlingJsonException(HttpMessageNotReadableException exception) {

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .success(false)
                .code(ResultCode.INVALID_REQUEST_FORMAT.getCode())
                .message(ResultCode.INVALID_REQUEST_FORMAT.name())
                .build();

        return ResponseEntity.badRequest().body(apiResponse);
    }
}
