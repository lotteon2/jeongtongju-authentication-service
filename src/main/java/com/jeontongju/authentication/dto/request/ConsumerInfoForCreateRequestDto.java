package com.jeontongju.authentication.dto.request;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ConsumerInfoForCreateRequestDto {

  @NotNull private Long memberId;

  @NotNull private String email;

  @NotNull private String name;

  public static ConsumerInfoForCreateRequestDto toDto(Long memberId, String email, String name) {
    return ConsumerInfoForCreateRequestDto.builder()
        .memberId(memberId)
        .email(email)
        .name(name)
        .build();
  }
}
