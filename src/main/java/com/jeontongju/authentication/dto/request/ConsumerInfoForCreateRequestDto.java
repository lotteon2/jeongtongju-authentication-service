package com.jeontongju.authentication.dto.request;

import com.jeontongju.authentication.dto.response.ImpAuthInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ConsumerInfoForCreateRequestDto {

  private Long consumerId;
  private String email;
  private String name;
  private String phoneNumber;

  public static ConsumerInfoForCreateRequestDto toDto(
          Long consumerId, String email, ImpAuthInfo impAuthInfo) {
    return ConsumerInfoForCreateRequestDto.builder()
        .consumerId(consumerId)
        .email(email)
        .name(impAuthInfo.getName())
        .phoneNumber(impAuthInfo.getPhone())
        .build();
  }
}
