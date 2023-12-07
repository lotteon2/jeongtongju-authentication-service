package com.jeontongju.authentication.exceptionhandler;

import com.jeontongju.authentication.dto.ErrorFormat;
import com.jeontongju.authentication.exception.DuplicateEmailException;
import com.jeontongju.authentication.utils.CustomErrMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
public class MemberRestControllerAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ErrorFormat> handleDuplicateEmail() {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(status.value())
        .body(
            ErrorFormat.builder()
                .code(status.value())
                .message(status.name())
                .detail(CustomErrMessage.EMAIL_ALREADY_IN_USE)
                .failure("DUPLICATED_EMAIL")
                .build());
  }
}
