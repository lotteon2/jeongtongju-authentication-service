package com.jeontongju.authentication.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImpTokenResponse {
  private Integer code;
  private String message;
  private ImpTokenInfo response;
}
