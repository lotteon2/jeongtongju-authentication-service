package com.jeontongju.authentication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ConsumerInfoForCreateBySnsRequestDto {

  private Long consumerId;
  private String email;
  private String profileImageUrl;

  public static ConsumerInfoForCreateBySnsRequestDto toDto(
      Long consumerId, String email, String profileImageUrl) {
    return ConsumerInfoForCreateBySnsRequestDto.builder()
        .consumerId(consumerId)
        .email(email)
        .profileImageUrl(profileImageUrl)
        .build();
  }
}
