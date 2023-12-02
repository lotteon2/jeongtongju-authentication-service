package com.jeontongju.authentication.security.jwt;

import com.jeontongju.authentication.security.MemberDetails;
import com.jeontongju.authentication.security.MemberDetailsService;
import com.jeontongju.authentication.security.jwt.token.JwtAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

  private final MemberDetailsService memberDetailsService;

  private final PasswordEncoder passwordEncoder;

  public JwtAuthenticationProvider(MemberDetailsService memberDetailsService) {
    this.memberDetailsService = memberDetailsService;
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    log.info("JwtAuthenticationProvider's authenticate executes");
    String username = authentication.getName();
    String password = authentication.getCredentials().toString();

    MemberDetails memberDetails = memberDetailsService.loadUserByUsername(username);

    if (!passwordEncoder.matches(password, memberDetails.getPassword())) {
      throw new BadCredentialsException("인증에 실패했습니다.");
    }

    return JwtAuthenticationToken.authenticated(
        memberDetails, null, memberDetails.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authenticationType) {
    return authenticationType.equals(JwtAuthenticationToken.class);
  }
}
