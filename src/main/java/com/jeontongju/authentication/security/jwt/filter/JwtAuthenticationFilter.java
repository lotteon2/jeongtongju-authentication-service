package com.jeontongju.authentication.security.jwt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeontongju.authentication.dto.request.MemberInfoForSignInRequestDto;
import com.jeontongju.authentication.dto.response.JwtAccessTokenResponse;
import com.jeontongju.authentication.dto.temp.ResponseFormat;
import com.jeontongju.authentication.exception.DuplicateAuthenticationException;
import com.jeontongju.authentication.security.MemberDetails;
import com.jeontongju.authentication.security.jwt.JwtTokenProvider;
import com.jeontongju.authentication.security.jwt.token.JwtAuthenticationToken;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
  private final RedisTemplate<String, String> redisTemplate;

  public JwtAuthenticationFilter(
      AuthenticationManager authenticationManager,
      JwtTokenProvider jwtTokenProvider,
      RedisTemplate<String, String> redisTemplate) {
    super(DEFAULT_FILTER_PROCESSES_URL, authenticationManager);
    this.jwtTokenProvider = jwtTokenProvider;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

    String platform = request.getHeader("Sec-Ch-Ua-Platform");

    if (isAlreadyAuthenticated()) {
      throw new DuplicateAuthenticationException();
    }

    log.info("[JwtAuthenticationFilter's attemptAuthentication executes]: attempt to sign-in..");

    try {
      MemberInfoForSignInRequestDto signInRequestDto =
          objectMapper.readValue(request.getInputStream(), MemberInfoForSignInRequestDto.class);

      JwtAuthenticationToken jwtAuthenticationToken =
          JwtAuthenticationToken.unauthenticated(
              signInRequestDto.getEmail(),
              signInRequestDto.getPassword(),
              signInRequestDto.getMemberRole().name(),
              platform);

      log.info("[AuthenticationManager's authenticate executes]");
      return this.getAuthenticationManager().authenticate(jwtAuthenticationToken);
    } catch (IOException e) {
      log.error("[Authentication Fail]={}", e.getMessage());
      throw new AuthenticationServiceException("잘못된 JSON 요청 형식입니다.");
    }
  }

  private boolean isAlreadyAuthenticated() {
    SecurityContext securityContext = SecurityContextHolder.getContext();
    if (securityContext == null) {
      return false;
    }

    Authentication authentication = securityContext.getAuthentication();
    if (authentication == null) {
      return false;
    }

    return authentication.getClass().equals(JwtAuthenticationToken.class);
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain,
      Authentication authResult)
      throws IOException {

    log.info("[Successful sign-in]!!");
    String jwtToken = jwtTokenProvider.createToken(authResult);
    MemberDetails memberDetails = (MemberDetails) authResult.getPrincipal();
    String jwtRefreshToken =
        jwtTokenProvider.createRefreshToken(memberDetails.getMember().getMemberId());

    //    ResponseCookie cookie =
    //        ResponseCookie.from("refreshToken", jwtRefreshToken)
    //            .domain(".jeontongju-dev.shop")
    //            .path("/")
    //            .sameSite("None")
    //            .httpOnly(true)
    //            .secure(true)
    //            .maxAge(21600000)
    //            .build();

    //    response.addHeader("Set-Cookie", cookie.toString());

    response.addHeader("Authorization", "Bearer " + jwtToken);

    response.setContentType("application/json; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");

    JwtAccessTokenResponse jwtAccessTokenResponse =
        JwtAccessTokenResponse.builder()
            .accessToken("Bearer " + jwtToken)
            .refreshToken("Bearer " + jwtRefreshToken)
            .build();

    String memberRole = memberDetails.getMember().getMemberRoleEnum().name();
    String refreshKey =
        memberRole + "_" + ((MemberDetails) authResult.getPrincipal()).getUsername();

    // refresh token 저장 in redis
    ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
    stringStringValueOperations.set(refreshKey, jwtRefreshToken);

    objectMapper.writeValue(
        response.getWriter(),
        ResponseFormat.<JwtAccessTokenResponse>builder()
            .code(HttpStatus.OK.value())
            .message(HttpStatus.OK.name())
            .detail("일반 로그인 성공")
            .data(jwtAccessTokenResponse)
            .build());
  }

  @Override
  protected void unsuccessfulAuthentication(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
      throws IOException, ServletException {

    log.info(failed.getMessage());
    response.setContentType("application/json");
    response.sendError(HttpStatus.BAD_REQUEST.value(), failed.getMessage());
  }
}
