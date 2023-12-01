package com.jeontongju.authentication.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final MemberDetailsService memberDetailsService;

    private final PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(MemberDetailsService memberDetailsService) {
        this.memberDetailsService = memberDetailsService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Authentication authenticate(Authentication authentication)
        throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        MemberDetails memberDetails = memberDetailsService.loadUserByUsername(username);

        if (!passwordEncoder.matches(password, memberDetails.getPassword())) {
            throw new BadCredentialsException("인증에 실패했습니다.");
        }

        return new UsernamePasswordAuthenticationToken(
            username,
            password,
            memberDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType.equals(UsernamePasswordAuthenticationToken.class);
    }
}
