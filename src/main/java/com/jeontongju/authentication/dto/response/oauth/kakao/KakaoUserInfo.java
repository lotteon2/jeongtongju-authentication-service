package com.jeontongju.authentication.dto.response.oauth.kakao;

import com.jeontongju.authentication.dto.response.oauth.OAuth2UserInfo;
import com.jeontongju.authentication.enums.SnsTypeEnum;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

  private String oauthId;
  private Map<String, Object> attributes;

  public KakaoUserInfo(Map<String, Object> attributes, String oauthId) {
    this.attributes = attributes;
    this.oauthId = oauthId;
  }

  @Override
  public String getProviderId() {

    return oauthId;
  }

  @Override
  public String getProvider() {
    return SnsTypeEnum.KAKAO.name();
  }

  @Override
  public String getEmail() {

    Map<String, String> attributesKakaoAccount = (Map) attributes.get("kakao_account");
    return attributesKakaoAccount.get("email");
  }

  @Override
  public String getProfileImageUrl() {

    Map<String, String> attributesProperties = (Map) attributes.get("properties");
    return attributesProperties.get("profile_image");
  }
}
