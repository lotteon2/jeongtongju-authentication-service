package com.jeontongju.authentication.dto.request;

import javax.validation.constraints.Email;
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
public class EmailInfoForAuthRequestDto {

  @NotNull
  @Email(message = "이메일을 올바른 형식으로 입력해주세요.")
  private String email;

  @NotNull
  private String memberRole;
}
