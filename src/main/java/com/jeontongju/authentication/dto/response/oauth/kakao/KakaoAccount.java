package com.jeontongju.authentication.dto.response.oauth.kakao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoAccount {

  private Profile profile;
  private Boolean is_email_verified;
  private String email;
}
