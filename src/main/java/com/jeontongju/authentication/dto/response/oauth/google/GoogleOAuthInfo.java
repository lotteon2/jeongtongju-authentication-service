package com.jeontongju.authentication.dto.response.oauth.google;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleOAuthInfo {

  private String id;
  private String email;
  private Boolean verified_email;
  private String picture;
}
