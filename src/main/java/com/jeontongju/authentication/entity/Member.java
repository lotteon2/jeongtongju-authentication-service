package com.jeontongju.authentication.entity;

import static javax.persistence.GenerationType.IDENTITY;

import com.jeontongju.authentication.enums.MemberRole;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

  @Column(name = "member_role")
  private MemberRole memberRole;

  @Column(name = "username")
  private String username;

  @Column(name = "password")
  private String password;

  @Column(name = "is_first_login")
  private Boolean isFirstLogin;

  @Column(name = "is_adult")
  private Boolean isAdult;

  @Column(name = "is_deleted")
  private Boolean isDeleted;
}
