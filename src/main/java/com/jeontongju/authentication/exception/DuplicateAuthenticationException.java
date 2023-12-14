package com.jeontongju.authentication.exception;

import com.jeontongju.authentication.utils.CustomErrMessage;
import org.springframework.security.authentication.AuthenticationServiceException;

public class DuplicateAuthenticationException extends AuthenticationServiceException {

  public DuplicateAuthenticationException() {
    super(CustomErrMessage.DUPLICATED_AUTHENTICATION);
  }
}
