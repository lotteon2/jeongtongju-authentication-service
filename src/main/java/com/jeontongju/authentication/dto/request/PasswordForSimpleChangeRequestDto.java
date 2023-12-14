package com.jeontongju.authentication.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PasswordForSimpleChangeRequestDto {

  @NotNull
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
      message = "비밀번호 형식에 맞게 입력해주세요")
  @Size(min = 8, max = 16, message = "비밀번호 형식에 맞게 입력해주세요")
  private String newPassword;
}
