package com.jeontongju.authentication.exception;

public class NotAdminAccessDeniedException extends RuntimeException {

  public NotAdminAccessDeniedException(String msg) {
    super(msg);
  }
}
