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
public class SellerInfoForSignUpRequestDto {

  @NotNull @Email private String email;

  @NotNull
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
      message = "사용자 이메일 또는 비밀번호 형식이 잘못되었습니다.")
  @Size(min = 8, max = 16, message = "회원가입 형식에 맞게 입력해주세요")
  private String password;

  @NotNull private String storeName;

  @NotNull private String storeDescription;

  @NotNull private String storeImageUrl;

  @NotNull private String storePhoneNumber;

  @NotNull private String impUid;
}
