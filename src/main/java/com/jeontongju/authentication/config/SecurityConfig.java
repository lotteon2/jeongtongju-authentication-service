package com.jeontongju.authentication.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.jeontongju.authentication.security.jwt.JwtAuthenticationProvider;
import com.jeontongju.authentication.security.jwt.JwtTokenProvider;
import com.jeontongju.authentication.security.jwt.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CorsFilter corsFilter;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtAuthenticationProvider jwtAuthenticationProvider;
  private final RedisTemplate<String, String> redisTemplate;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .formLogin()
        .disable()
        .httpBasic()
        .disable()
        .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(STATELESS));

    http.addFilter(corsFilter).apply(new MyCustomDsl());

    http.authorizeRequests(
        authz ->
            authz
                .antMatchers("/api/password/auth")
                .permitAll()
                .antMatchers("/api/access-token")
                .permitAll()
                .antMatchers("/api/email/auth")
                .permitAll()
                .antMatchers("/api/sign-up/email/auth")
                .permitAll()
                .antMatchers("/api/consumers/sign-up")
                .permitAll()
                .antMatchers("/api/sellers/sign-up")
                .permitAll()
                .antMatchers("/api/sign-in")
                .permitAll()
                .antMatchers("/**")
                .hasAnyRole("CONSUMER", "SELLER", "ADMIN")
                .anyRequest()
                .authenticated());

    return http.build();
  }

  public class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
    @Override
    public void configure(HttpSecurity http) throws Exception {

      AuthenticationManager authenticationManager =
          http.getSharedObject(AuthenticationManager.class);
      JwtAuthenticationFilter jwtAuthenticationFilter =
          new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider, redisTemplate);
      // UsernamePasswordAuthenticationFilter 직전
      http.addFilterAfter(jwtAuthenticationFilter, LogoutFilter.class)
          .authenticationProvider(jwtAuthenticationProvider);
    }
  }
}
