package com.jeontongju.authentication.utils;

import com.jeontongju.authentication.dto.response.ImpAuthInfo;
import com.jeontongju.authentication.dto.response.ImpAuthResponse;
import com.jeontongju.authentication.dto.response.ImpTokenResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class Auth19Manager {
  private static final String GET_ACCESS_TOKEN_URL = "https://api.iamport.kr/users/getToken";
  private static final String GET_AUTH_INFO_PREFIX_URL = "https://api.iamport.kr/certifications/";
  private static String impKey;
  private static String impSecret;

  public Auth19Manager(Environment env) {
    impKey = env.getProperty("store.imp.key");
    impSecret = env.getProperty("store.imp.secret");
  }

  public static ImpAuthInfo authenticate19(String impUid) throws IOException, JSONException {

    HttpEntity<Map<String, String>> requestEntity;
    Map<String, String> body = new HashMap<>();
    body.put("imp_key", impKey);
    body.put("imp_secret", impSecret);
    requestEntity = new HttpEntity<>(body, getAccessTokenHeaders());

    RestTemplate template = new RestTemplate();

    // access_token 받아오기
    ImpTokenResponse impTokenResponse =
        template.postForObject(GET_ACCESS_TOKEN_URL, requestEntity, ImpTokenResponse.class);

    String accessToken = impTokenResponse.getResponse().getAccess_token();

    requestEntity = new HttpEntity<>(getAuthInfoHeaders(accessToken));

    // 인증정보 조회하기
    ResponseEntity<ImpAuthResponse> impAuthInfoResponseEntity =
        template.exchange(
            GET_AUTH_INFO_PREFIX_URL + impUid,
            HttpMethod.GET,
            requestEntity,
            ImpAuthResponse.class);

    ImpAuthResponse impAuthResponse = impAuthInfoResponseEntity.getBody();

    String birthday = impAuthResponse.getResponse().getBirthday();

    if (!isAdult(birthday)) {
      throw new RuntimeException();
    }

    return impAuthResponse.getResponse();
  }

  private static Boolean isAdult(String birthday) {
    LocalDate birthDate =
        LocalDate.of(
            Integer.parseInt(birthday.substring(0, 4)),
            Integer.parseInt(birthday.substring(5, 7)),
            Integer.parseInt(birthday.substring(8, birthday.length())));
    long yearsGap = ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    return yearsGap >= 20;
  }

  private static HttpHeaders getAccessTokenHeaders() {

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    return headers;
  }

  private static HttpHeaders getAuthInfoHeaders(String accessToken) {

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + accessToken);
    return headers;
  }
}
