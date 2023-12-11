package com.jeontongju.authentication.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.jeontongju.authentication.enums.MemberRoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PasswordForChangeRequestDto {

    @NotNull private String email;

    @NotNull private MemberRoleEnum memberRole;

    @NotNull
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$",
            message = "회원가입 형식에 맞게 입력해주세요")
    @Size(min = 8, max = 16, message = "회원가입 형식에 맞게 입력해주세요")
    private String newPassword;
}
