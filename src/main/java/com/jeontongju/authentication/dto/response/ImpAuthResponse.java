package com.jeontongju.authentication.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class ImpAuthResponse {
  private Integer code;
  private String message;
  private ImpAuthInfo response;
}
