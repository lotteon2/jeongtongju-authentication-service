package com.jeontongju.authentication.entity;

import static javax.persistence.GenerationType.IDENTITY;

import com.jeontongju.authentication.enums.MemberRoleEnum;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(
    name = "member",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "memberUnique",
          columnNames = {"member_role", "username"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Member {
  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "member_id")
  private Long memberId;

  @Enumerated(EnumType.STRING)
  @Column(name = "member_role", nullable = false)
  private MemberRoleEnum memberRoleEnum;

  @Column(name = "username", nullable = false)
  private String username;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "is_first_login", nullable = false)
  @Builder.Default
  private Boolean isFirstLogin = true;

  @Column(name = "is_adult", nullable = false)
  @Builder.Default
  private Boolean isAdult = false;

  @Column(name = "is_deleted", nullable = false)
  @Builder.Default
  private Boolean isDeleted = false;

  public static Member register(String email, String password, MemberRoleEnum role) {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    return Member.builder()
        .username(email)
        .password(passwordEncoder.encode(password))
        .memberRoleEnum(role)
        .build();
  }
}
