package com.jeontongju.authentication.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ConsumerInfoForSignUpRequestDto {

  @NotNull
  @Email(message = "회원가입 형식에 맞게 입력해주세요")
  private String email;

  @NotNull
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
      message = "회원가입 형식에 맞게 입력해주세요")
  @Size(min = 8, max = 16, message = "회원가입 형식에 맞게 입력해주세요")
  private String password;

  @NotNull private String impUid;
}
