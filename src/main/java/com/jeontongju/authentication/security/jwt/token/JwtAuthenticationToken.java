package com.jeontongju.authentication.security.jwt.token;

import java.util.Collection;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

@Getter
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  private final Object principal;
  private final Object credentials;
  private String role;
  private final String platform;

  public JwtAuthenticationToken(
      Object principal, Object credentials, String role, String platform) {

    super(null);
    this.principal = principal;
    this.credentials = credentials;
    this.role = role;
    this.platform = platform;
    setAuthenticated(false);
  }

  public JwtAuthenticationToken(
      Object principal,
      Object credentials,
      Collection<? extends GrantedAuthority> authorities,
      String platform) {

    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    this.platform = platform;
    super.setAuthenticated(true);
  }

  // 인증 처리전 호출
  public static JwtAuthenticationToken unauthenticated(
      Object principal, Object credentials, String role, String platform) {
    return new JwtAuthenticationToken(principal, credentials, role, platform);
  }

  // 인증 처리후 호출
  public static JwtAuthenticationToken authenticated(
      Object principal,
      Object credentials,
      Collection<? extends GrantedAuthority> authorities,
      String platform) {
    return new JwtAuthenticationToken(principal, credentials, authorities, platform);
  }

  @Override
  public Object getCredentials() {
    return this.credentials;
  }

  @Override
  public Object getPrincipal() {
    return this.principal;
  }
}
