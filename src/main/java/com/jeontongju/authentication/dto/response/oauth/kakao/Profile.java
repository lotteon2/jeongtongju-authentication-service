package com.jeontongju.authentication.dto.response.oauth.kakao;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Profile {

  private String thumbnail_image_url;
  private String profile_image_url;
  private Boolean is_default_image;
}
