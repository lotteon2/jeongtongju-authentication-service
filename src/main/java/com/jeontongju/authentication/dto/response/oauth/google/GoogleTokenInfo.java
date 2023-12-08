package com.jeontongju.authentication.dto.response.oauth.google;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleTokenInfo {

  private String token_type;
  private String access_token;
  private Integer expires_in;
  private String refresh_token;
  private String scope;
}
