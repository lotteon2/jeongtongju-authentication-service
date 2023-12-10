package com.jeontongju.authentication.dto.request;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PasswordForCheckRequestDto {

  private String originalPassword;
}