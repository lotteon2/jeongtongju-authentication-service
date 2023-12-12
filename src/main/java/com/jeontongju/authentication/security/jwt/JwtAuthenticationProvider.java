package com.jeontongju.authentication.security.jwt;

import com.jeontongju.authentication.security.MemberDetails;
import com.jeontongju.authentication.security.MemberDetailsService;
import com.jeontongju.authentication.security.jwt.token.JwtAuthenticationToken;
import com.jeontongju.authentication.utils.CustomErrMessage;
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

    JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
    String memberRole = jwtAuthenticationToken.getRole();

    MemberDetails memberDetails = memberDetailsService.loadUserByUsername(username);

    // 탈퇴한 회원
    if (!memberDetails.isEnabled()) {
      throw new AuthenticationException(CustomErrMessage.DISABLED_MEMBER) {
        @Override
        public String getMessage() {
          return super.getMessage();
        }
      };
    }

    // 소비자 회원가입 페이지에서 온 요청은 셀러 로그인 불가, 반대도 불가
    if (!memberRole.equals(memberDetails.getMember().getMemberRoleEnum().name())) {
      throw new BadCredentialsException(CustomErrMessage.NOT_AUTHENTICATED);
    }

    if (!passwordEncoder.matches(password, memberDetails.getPassword())) {
      throw new BadCredentialsException(CustomErrMessage.NOT_CORRESPOND_CREDENTIALS);
    }

    return JwtAuthenticationToken.authenticated(
        memberDetails, null, memberDetails.getAuthorities());
  }

  @Override
  public boolean supports(Class<?> authenticationType) {
    return authenticationType.equals(JwtAuthenticationToken.class);
  }
}
