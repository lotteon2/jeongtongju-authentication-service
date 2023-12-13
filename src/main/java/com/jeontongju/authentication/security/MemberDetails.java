package com.jeontongju.authentication.security;

import com.jeontongju.authentication.entity.Member;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class MemberDetails implements UserDetails, OAuth2User {

  private Member member;
  private Map<String, Object> attributes;
  
  public MemberDetails(Member member) {
    this.member = member;
  }

  // OAuth2.0 로그인 시 사용
  public MemberDetails(Member member, Map<String, Object> attributes) {
    this.member = member;
    this.attributes = attributes;
  }
  
  // 리소스 서버로부터 받는 회원 정보
  @Override
  public Map<String, Object> getAttributes() {
    return this.attributes;
  }

  // primary key
  @Override
  public String getName() {
    return member.getMemberId().toString();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> collect = new ArrayList<>();
    collect.add(
        new GrantedAuthority() {
          @Override
          public String getAuthority() {
            return member.getMemberRoleEnum().name();
          }
        });
    return collect;
  }

  @Override
  public String getPassword() {
    return member.getPassword();
  }

  @Override
  public String getUsername() {
    return member.getUsername();
  }

  @Override
  public boolean isAccountNonExpired() {
    return false;
  }

  @Override
  public boolean isAccountNonLocked() {
    return false;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return false;
  }

  @Override
  public boolean isEnabled() {
    return !member.getIsDeleted();
  }
}
