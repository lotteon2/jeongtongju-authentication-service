package com.jeontongju.authentication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ConsumerInfoForCreateByKakaoRequestDto {

  private Long consumerId;
  private String email;
  private String profileImageUrl;

  public static ConsumerInfoForCreateByKakaoRequestDto toDto(
      Long consumerId, String email, String profileImageUrl) {
    return ConsumerInfoForCreateByKakaoRequestDto.builder()
        .consumerId(consumerId)
        .email(email)
        .profileImageUrl(profileImageUrl)
        .build();
  }
}
