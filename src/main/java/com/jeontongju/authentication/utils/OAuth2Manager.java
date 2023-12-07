package com.jeontongju.authentication.utils;

import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoOAuthInfo;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoTokenInfo;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class OAuth2Manager {

  private static String CLIENT_ID;
  private static final String GET_OAUTH_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
  private static final String REGISTERED_REDIRECT_URI =
      "http://localhost:8020/api/sign-in/oauth2/code/kakao";
  private static final String GET_OAUTH_INFO_URL = "https://kapi.kakao.com/v2/user/me";

  public OAuth2Manager(Environment env) {
    OAuth2Manager.CLIENT_ID = env.getProperty("oauth2.client-id");
  }

  public static KakaoOAuthInfo authenticateByKakao(String code) {

    RestTemplate template = new RestTemplate();

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", CLIENT_ID);
    body.add("redirect_uri", REGISTERED_REDIRECT_URI);
    body.add("code", code);

    HttpEntity<Map> requestEntity = new HttpEntity<>(body, getHeaders());

    // 토큰 가져오기
    KakaoTokenInfo kakaoTokenInfo =
        template.postForObject(GET_OAUTH_TOKEN_URL, requestEntity, KakaoTokenInfo.class);

    log.info("kakaoTokenInfo: " + kakaoTokenInfo);

    requestEntity =
        new HttpEntity<>(
            getHeadersForOAuthInfo(
                kakaoTokenInfo.getToken_type(), kakaoTokenInfo.getAccess_token()));

    // 사용자 정보 가져오기
    return template.postForObject(GET_OAUTH_INFO_URL, requestEntity, KakaoOAuthInfo.class);
  }

  private static HttpHeaders getHeaders() {

    HttpHeaders headers = new HttpHeaders();

    headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
    return headers;
  }

  private static HttpHeaders getHeadersForOAuthInfo(String tokenType, String accessToken) {

    HttpHeaders headers = new HttpHeaders();
    String authorization = tokenType + " " + accessToken;

    headers.set("Authorization", authorization);
    headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
    return headers;
  }
}
