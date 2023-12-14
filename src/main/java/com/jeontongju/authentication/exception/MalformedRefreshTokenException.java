package com.jeontongju.authentication.exception;

public class MalformedRefreshTokenException extends RuntimeException {

  public MalformedRefreshTokenException(String msg) {
    super(msg);
  }
}
