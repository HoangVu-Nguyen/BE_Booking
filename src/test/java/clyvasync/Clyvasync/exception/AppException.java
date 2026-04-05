package clyvasync.Clyvasync.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

  private ResultCode errorCode;

  public AppException(ResultCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}