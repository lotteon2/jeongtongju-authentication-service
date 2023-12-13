package com.jeontongju.authentication.dto.response.oauth.google;

import com.jeontongju.authentication.dto.response.oauth.OAuth2UserInfo;
import com.jeontongju.authentication.enums.SnsTypeEnum;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {

  private Map<String, Object> attributes;

  public GoogleUserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String getProviderId() {
    return null;
  }

  @Override
  public String getProvider() {
    return SnsTypeEnum.GOOGLE.name();
  }

  @Override
  public String getEmail() {
    return null;
  }

  @Override
  public String getProfileImageUrl() {
    return null;
  }
}
