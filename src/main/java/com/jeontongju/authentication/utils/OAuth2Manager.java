package com.jeontongju.authentication.utils;

import com.jeontongju.authentication.dto.response.oauth.google.GoogleOAuthInfo;
import com.jeontongju.authentication.dto.response.oauth.google.GoogleTokenInfo;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoOAuthInfo;
import com.jeontongju.authentication.dto.response.oauth.kakao.KakaoTokenInfo;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class OAuth2Manager {

  private static String KAKAO_CLIENT_ID;
  private static String GOOGLE_CLIENT_ID;
  private static String GOOGLE_CLIENT_SECRET;
  private static String REGISTERED_REDIRECT_URI_KAKAO;
  private static String REGISTERED_REDIRECT_URI_GOOGLE;

  private static final String GET_OAUTH_TOKEN_URL_KAKAO = "https://kauth.kakao.com/oauth/token";
  private static final String GET_OAUTH_TOKEN_URL_GOOGLE = "https://oauth2.googleapis.com/token";
  private static final String GET_OAUTH_INFO_URL_KAKAO = "https://kapi.kakao.com/v2/user/me";
  private static final String GET_OAUTH_INFO_URL_GOOGLE =
      "https://www.googleapis.com/userinfo/v2/me";

  private static final RestTemplate template = new RestTemplate();

  public OAuth2Manager(Environment env) {

    OAuth2Manager.KAKAO_CLIENT_ID = env.getProperty("oauth2.kakao.client-id");
    OAuth2Manager.GOOGLE_CLIENT_ID = env.getProperty("oauth2.google.client-id");
    OAuth2Manager.GOOGLE_CLIENT_SECRET = env.getProperty("oauth2.google.client-secret");
    OAuth2Manager.REGISTERED_REDIRECT_URI_KAKAO = env.getProperty("oauth2.kakao.redirect-uri");
    OAuth2Manager.REGISTERED_REDIRECT_URI_GOOGLE = env.getProperty("oauth2.google.redirect-uri");
  }

  public static KakaoOAuthInfo authenticateByKakao(String code) {

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", KAKAO_CLIENT_ID);
    body.add("redirect_uri", REGISTERED_REDIRECT_URI_KAKAO);
    body.add("code", code);

    HttpEntity<Map> requestEntity = new HttpEntity<>(body, getHeaders());

    // 토큰 가져오기
    KakaoTokenInfo kakaoTokenInfo =
        template.postForObject(GET_OAUTH_TOKEN_URL_KAKAO, requestEntity, KakaoTokenInfo.class);

    log.info("kakaoTokenInfo: " + kakaoTokenInfo);

    requestEntity =
        new HttpEntity<>(
            getHeadersForOAuthInfo(
                kakaoTokenInfo.getToken_type(), kakaoTokenInfo.getAccess_token()));

    // 사용자 정보 가져오기
    return template.postForObject(GET_OAUTH_INFO_URL_KAKAO, requestEntity, KakaoOAuthInfo.class);
  }

  public static GoogleOAuthInfo authenticateByGoogle(String code) {

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("code", code);
    body.add("client_id", GOOGLE_CLIENT_ID);
    body.add("client_secret", GOOGLE_CLIENT_SECRET);
    body.add("redirect_uri", REGISTERED_REDIRECT_URI_GOOGLE);
    body.add("grant_type", "authorization_code");

    HttpEntity<Map> requestEntity = new HttpEntity<>(body, getHeaders());

    GoogleTokenInfo googleTokenInfo =
        template.postForObject(GET_OAUTH_TOKEN_URL_GOOGLE, requestEntity, GoogleTokenInfo.class);

    requestEntity =
        new HttpEntity<>(
            getHeadersForOAuthInfo(
                googleTokenInfo.getToken_type(), googleTokenInfo.getAccess_token()));

    return template
        .exchange(GET_OAUTH_INFO_URL_GOOGLE, HttpMethod.GET, requestEntity, GoogleOAuthInfo.class)
        .getBody();
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
