package com.jeontongju.authentication.utils;

import com.jeontongju.authentication.dto.response.ImpAuthInfoResponse;
import com.jeontongju.authentication.dto.response.ImpTokenResponse;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class Auth19Manager {
  private static final String GET_ACCESS_TOKEN_URL = "https://api.iamport.kr/users/getToken";
  private static final String GET_AUTH_INFO_PREFIX_URL = "https://api.iamport.kr/certifications/";

  private static String impKey;

  private static String impSecret;

  public Auth19Manager(Environment env) {
    impKey = env.getProperty("store.imp.key");
    impSecret = env.getProperty("store.imp.secret");
  }

  public static ImpAuthInfoResponse authenticate19(String impUid)
      throws IOException, JSONException {

    HttpEntity<Map> requestEntity;
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("imp_key", impKey);
    body.add("imp_secret", impSecret);
    requestEntity = new HttpEntity<>(body, getAccessTokenHeaders());

    RestTemplate template = new RestTemplate();

    // access_token 받아오기
    ImpTokenResponse impTokenResponse =
        template.postForObject(GET_ACCESS_TOKEN_URL, requestEntity, ImpTokenResponse.class);

    System.out.println("impTokenResponse: " + impTokenResponse);

    requestEntity = new HttpEntity<>(getAuthInfoHeaders(impTokenResponse.getAccess_token()));

    // 인증정보 조회하기
    ResponseEntity<ImpAuthInfoResponse> impAuthInfoResponseEntity =
        template.exchange(
            GET_AUTH_INFO_PREFIX_URL + impUid,
            HttpMethod.GET,
            requestEntity,
            ImpAuthInfoResponse.class);

    ImpAuthInfoResponse impAuthInfoResponse = impAuthInfoResponseEntity.getBody();

    if(!isAdult(impAuthInfoResponse.getBirth(), impAuthInfoResponse.getGender_digit())) {
      throw new RemoteException();
    }

    return impAuthInfoResponseEntity.getBody();
  }

  private static Boolean isAdult(String birth, String gender_digit) {
    String prefixBirthYear = gender_digit.equals("1") || gender_digit.equals("2") ? "19" : "20";
    String birthYear = prefixBirthYear + birth.substring(0, 2);

    LocalDate birthDay =
        LocalDate.of(
            Integer.parseInt(birthYear),
            Integer.parseInt(birth.substring(2, 4)),
            Integer.parseInt(birth.substring(4, 6)));
    long yearsGap = ChronoUnit.YEARS.between(birthDay, LocalDate.now());
    return yearsGap >= 20;
  }

  private static HttpHeaders getAccessTokenHeaders() {

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    return headers;
  }

  private static HttpHeaders getAuthInfoHeaders(String accessToken) {

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", accessToken);
    return headers;
  }
}
