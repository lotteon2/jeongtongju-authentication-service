package com.jeontongju.authentication.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeontongju.authentication.dto.response.JwtAccessTokenResponse;
import com.jeontongju.authentication.dto.temp.ResponseFormat;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {

    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json; charset=UTF-8");

    objectMapper.writeValue(
        response.getWriter(),
        ResponseFormat.<JwtAccessTokenResponse>builder()
            .code(HttpStatus.BAD_REQUEST.value())
            .message(HttpStatus.BAD_REQUEST.name())
            .detail("소셜 로그인 실패: " + exception.getMessage())
            .build());
  }
}
