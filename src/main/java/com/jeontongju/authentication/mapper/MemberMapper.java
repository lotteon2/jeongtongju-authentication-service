package com.jeontongju.authentication.mapper;

import com.jeontongju.authentication.entity.Member;
import com.jeontongju.authentication.enums.MemberRoleEnum;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

  public Member toEntity(String email, String password, MemberRoleEnum role) {

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    return Member.builder()
        .username(email)
        .password(passwordEncoder.encode(password))
        .memberRoleEnum(role)
        .build();
  }
}
