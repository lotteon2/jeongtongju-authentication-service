package com.jeontongju.authentication.exception;

public class NotValidRefreshTokenException extends RuntimeException {

  public NotValidRefreshTokenException(String msg) {
    super(msg);
  }
}
