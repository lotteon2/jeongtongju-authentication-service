package com.jeontongju.authentication.exceptionhandler;

import com.jeontongju.authentication.dto.ResponseFormat;
import com.jeontongju.authentication.exception.*;
import com.jeontongju.authentication.utils.CustomErrMessage;
import java.io.IOException;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class MemberRestControllerAdvice extends ResponseEntityExceptionHandler {

  @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
  public ResponseEntity<ResponseFormat<Void>> handleUsernamePasswordException() {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(CustomErrMessage.NOT_CORRESPOND_CREDENTIALS)
                .failure("NOT_CORRESPOND_CREDENTIALS")
                .build());
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ResponseFormat<Void>> handleNotFoundEntity() {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(CustomErrMessage.NOT_FOUND_MEMBER)
                .build());
  }

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ResponseFormat<Void>> handleDuplicateEmail() {

    HttpStatus status = HttpStatus.OK;

    return ResponseEntity.status(status.value())
        .body(
            ResponseFormat.<Void>builder()
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
  public ResponseEntity<ResponseFormat<Void>> handleNotValidRefreshToken(
      HttpServletResponse response) throws IOException {

    HttpStatus status = HttpStatus.UNAUTHORIZED;

    return ResponseEntity.status(status.value())
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(CustomErrMessage.NOT_VALID_REFRESH_TOKEN)
                .failure("NOT_VALID_REFRESH_TOKEN")
                .build());
  }

  @ExceptionHandler(NotCorrespondPassword.class)
  public ResponseEntity<ResponseFormat<Void>> handleNotCorrespondPassword() {

    HttpStatus status = HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(status)
        .body(
            ResponseFormat.<Void>builder()
                .code(status.value())
                .message(status.name())
                .detail(CustomErrMessage.NOT_CORRESPOND_ORIGIN_PASSWORD)
                .build());
  }
}
