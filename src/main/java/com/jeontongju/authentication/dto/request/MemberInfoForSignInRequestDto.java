package com.jeontongju.authentication.dto.request;

import com.jeontongju.authentication.enums.MemberRoleEnum;
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
public class MemberInfoForSignInRequestDto {

  @NotNull @Email private String email;

  @NotNull
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
      message = "사용자 이메일 또는 비밀번호 형식이 잘못되었습니다.")
  @Size(min = 8, max = 16, message = "사용자 이메일 또는 비밀번호 형식이 잘못되었습니다.")
  private String password;

  @NotNull MemberRoleEnum memberRole;
}
