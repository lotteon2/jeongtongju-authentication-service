package com.jeontongju.authentication.exceptionhandler;

import com.jeontongju.authentication.dto.ErrorFormat;
import com.jeontongju.authentication.exception.*;
import com.jeontongju.authentication.utils.CustomErrMessage;
import java.io.IOException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class MemberRestControllerAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorFormat> handleNotFoundEntity() {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ErrorFormat.builder()
                .code(status.value())
                .message(status.name())
                .detail(CustomErrMessage.NOT_FOUND_MEMBER)
                .build());
  }

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

  @ExceptionHandler({
    ExpiredRefreshTokenException.class,
    MalformedRefreshTokenException.class,
    NotValidRefreshTokenException.class
  })
  public void handleNotValidRefreshToken(HttpServletResponse response) throws IOException {

    response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
    response.setHeader("Location", "https://test-jeontongju-jumo.netlify.app/");
  }

  @ExceptionHandler(NotCorrespondPassword.class)
  public ResponseEntity<ErrorFormat> handleNotCorrespondPassword() {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(status)
        .body(
            ErrorFormat.builder()
                .code(status.value())
                .message(status.name())
                .detail(CustomErrMessage.NOT_CORRESPOND_ORIGIN_PASSWORD)
                .build());
  }
}
