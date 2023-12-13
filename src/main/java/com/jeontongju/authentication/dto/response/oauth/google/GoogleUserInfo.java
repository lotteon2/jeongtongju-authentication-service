package com.jeontongju.authentication.dto.response.oauth.google;

import com.jeontongju.authentication.dto.response.oauth.OAuth2UserInfo;
import com.jeontongju.authentication.enums.SnsTypeEnum;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {

  private String oauthId;
  private Map<String, Object> attributes;

  public GoogleUserInfo(Map<String, Object> attributes, String oauthId) {
    this.attributes = attributes;
    this.oauthId = oauthId;
  }

  @Override
  public String getProviderId() {
    return this.oauthId;
  }

  @Override
  public String getProvider() {
    return SnsTypeEnum.GOOGLE.name();
  }

  @Override
  public String getEmail() {

    return (String) attributes.get("email");
  }

  @Override
  public String getProfileImageUrl() {

    return (String) attributes.get("picture");
  }
}
