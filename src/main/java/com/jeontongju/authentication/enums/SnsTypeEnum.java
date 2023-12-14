package com.jeontongju.authentication.enums;

public enum SnsTypeEnum {
  KAKAO("KAKAO"),
  GOOGLE("GOOGLE");

  private final String value;

  SnsTypeEnum(String value) {
    this.value = value;
  }
}
