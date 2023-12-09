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

  public JwtAuthenticationToken(Object principal, Object credentials, String role) {
    super(null);
    this.principal = principal;
    this.credentials = credentials;
    this.role = role;
    setAuthenticated(false);
  }

  public JwtAuthenticationToken(
      Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    this.credentials = credentials;
    super.setAuthenticated(true);
  }

  // 인증 처리전 호출
  public static JwtAuthenticationToken unauthenticated(Object principal, Object credentials, String role) {
    return new JwtAuthenticationToken(principal, credentials, role);
  }

  // 인증 처리후 호출
  public static JwtAuthenticationToken authenticated(
      Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
    return new JwtAuthenticationToken(principal, credentials, authorities);
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
