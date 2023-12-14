package com.jeontongju.authentication.dto.response.oauth;

public interface OAuth2UserInfo {

  String getProviderId();

  String getProvider();

  String getEmail();

  String getProfileImageUrl();
}