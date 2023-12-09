package com.jeontongju.authentication.exception;


public class ExpiredRefreshTokenException extends RuntimeException {

  public ExpiredRefreshTokenException(String msg) {
    super(msg);
  }
}
