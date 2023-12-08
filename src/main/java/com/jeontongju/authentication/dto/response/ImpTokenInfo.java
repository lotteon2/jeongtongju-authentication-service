package com.jeontongju.authentication.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class ImpTokenInfo {

  private String access_token;
  private Long now;
  private Long expired_at;
}
