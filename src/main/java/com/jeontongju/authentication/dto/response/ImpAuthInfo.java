package com.jeontongju.authentication.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImpAuthInfo {
  private String name;
  private String birthday;
  private String phone;
}
