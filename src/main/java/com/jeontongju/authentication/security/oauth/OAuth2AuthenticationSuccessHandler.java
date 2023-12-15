package com.jeontongju.authentication.security.oauth;

import java.io.IOException;
import java.util.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeontongju.authentication.dto.temp.ResponseFormat;
import com.jeontongju.authentication.dto.response.JwtAccessTokenResponse;
import com.jeontongju.authentication.security.MemberDetails;
import com.jeontongju.authentication.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    MemberDetails memberDetails = (MemberDetails) oAuth2User;
    Collection<? extends GrantedAuthority> authorities = memberDetails.getAuthorities();

    Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
    GrantedAuthority authority = iterator.next();
    String memberRole = authority.getAuthority();

    String accessToken = jwtTokenProvider.createToken(authentication);
    String refreshToken = jwtTokenProvider.createRefreshToken(Long.parseLong(oAuth2User.getName()));
    String refreshKey = memberRole + "_" + memberDetails.getUsername();

    ValueOperations<String, String> stringStringValueOperations = redisTemplate.opsForValue();
    stringStringValueOperations.set(refreshKey, refreshToken);

    Cookie cookie = new Cookie("refreshToken", refreshToken);
    cookie.setMaxAge(21600000);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    response.addCookie(cookie);

    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    response.sendRedirect("https://jeontongju-front-consumer.vercel.app/init/callback?code=" + accessToken);
  }
}
