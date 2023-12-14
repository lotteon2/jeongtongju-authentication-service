package com.jeontongju.authentication.dto.response.oauth.kakao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoTokenInfo {

  private String token_type;
  private String access_token;
  private Integer expires_in;
  private String refresh_token;
  private Integer refresh_token_expires_in;
}
