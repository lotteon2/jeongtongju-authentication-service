package com.jeontongju.authentication.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class ConsumerInfoForSignUpForRequestDto {

  @NotNull
  @Email(message = "회원가입 형식에 맞게 입력해주세요")
  private String email;

  @NotNull
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
      message = "회원가입 형식에 맞게 입력해주세요")
  @Size(min = 8, max = 16, message = "회원가입 형식에 맞게 입력해주세요")
  private String password;

  @NotNull
  @Size(max = 10, message = "회원가입 형식에 맞게 입력해주세요")
  private String name;
}
