package com.jeontongju.authentication.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImpTokenResponse {

  private Integer code;
  private String message;
  private ImpTokenInfo response;
}
