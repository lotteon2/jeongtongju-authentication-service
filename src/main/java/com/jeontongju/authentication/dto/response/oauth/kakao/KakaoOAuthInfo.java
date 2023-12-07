package com.jeontongju.authentication.dto.response.oauth.kakao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class KakaoOAuthInfo {

  private Long id;
  private KakaoAccount kakao_account;
}
