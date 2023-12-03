package com.jeontongju.authentication.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ImpAuthInfoResponse {

  private String name;
  private String gender;
  private String birth;
  private String gender_digit;
  private String unique_key;
  private String phone;
}
