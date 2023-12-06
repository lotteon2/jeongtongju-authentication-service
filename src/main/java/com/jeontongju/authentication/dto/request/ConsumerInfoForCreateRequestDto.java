package com.jeontongju.authentication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ConsumerInfoForCreateRequestDto {

  private Long memberId;

  private String email;

  private String name;

  public static ConsumerInfoForCreateRequestDto toDto(Long memberId, String email, String name) {
    return ConsumerInfoForCreateRequestDto.builder()
        .memberId(memberId)
        .email(email)
        .name(name)
        .build();
  }
}
