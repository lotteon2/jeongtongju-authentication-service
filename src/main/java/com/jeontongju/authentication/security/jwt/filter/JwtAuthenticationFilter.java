package com.jeontongju.authentication.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeontongju.authentication.dto.MemberInfoForSignInRequestDto;
import com.jeontongju.authentication.exception.DuplicateAuthenticationException;
import com.jeontongju.authentication.security.jwt.JwtTokenProvider;
import com.jeontongju.authentication.security.jwt.token.JwtAuthenticationToken;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j
public class JwtAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private static final AntPathRequestMatcher DEFAULT_FILTER_PROCESSES_URL =
      new AntPathRequestMatcher("/api/sign-in", HttpMethod.POST.name());

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final JwtTokenProvider jwtTokenProvider;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
    super(DEFAULT_FILTER_PROCESSES_URL, authenticationManager);
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    if(isAlreadyAuthenticated()) {
      throw new DuplicateAuthenticationException();
    }

    log.info("JwtAuthenticationFilter: attempt sign-in...");

    try {
      MemberInfoForSignInRequestDto signInRequestDto =
          objectMapper.readValue(request.getInputStream(), MemberInfoForSignInRequestDto.class);

      JwtAuthenticationToken jwtAuthenticationToken =
          JwtAuthenticationToken.unauthenticated(
              signInRequestDto.getEmail(), signInRequestDto.getPassword());

      log.info("AuthenticationManager's authenticate executes");
      return this.getAuthenticationManager().authenticate(jwtAuthenticationToken);
    } catch (IOException e) {
      log.info("Authentication Fail!");
      throw new AuthenticationServiceException("잘못된 JSON 요청 형식입니다.");
    }
  }

  private boolean isAlreadyAuthenticated() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    if(securityContext == null) {
      return false;
    }

    Authentication authentication = securityContext.getAuthentication();
    if(authentication == null) {
      return false;
    }

    return authentication.getClass().equals(JwtAuthenticationToken.class);
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain,
      Authentication authResult) {

    log.info("Successful sing-up!");
    String jwtToken = jwtTokenProvider.createToken(authResult);

    response.addHeader("Authorization", "Bearer " + jwtToken);

    Cookie cookie = new Cookie("jwt-token", jwtToken);
    cookie.setMaxAge(1800000);
    cookie.setPath("/");
    cookie.setSecure(false);
    response.addCookie(cookie);
  }
}
