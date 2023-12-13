package com.jeontongju.authentication.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.jeontongju.authentication.security.jwt.JwtAuthenticationProvider;
import com.jeontongju.authentication.security.jwt.JwtTokenProvider;
import com.jeontongju.authentication.security.jwt.filter.InitialAuthenticationFilter;
import com.jeontongju.authentication.security.jwt.filter.JwtAuthenticationFilter;
import com.jeontongju.authentication.security.oauth.MemberOAuth2UserService;
import com.jeontongju.authentication.security.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CorsFilter corsFilter;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtAuthenticationProvider jwtAuthenticationProvider;
  private final MemberOAuth2UserService memberOAuth2UserService;
  private final RedisTemplate<String, String> redisTemplate;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .formLogin()
        .disable()
        .httpBasic()
        .disable()
        .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(STATELESS));

    http.oauth2Login()
        .userInfoEndpoint()
        .userService(memberOAuth2UserService)
        .and()
        .successHandler(oAuth2AuthenticationSuccessHandler);

    http.addFilter(corsFilter).apply(new MyCustomDsl());

    http.authorizeRequests(authz -> authz.anyRequest().permitAll());
    return http.build();
  }

  public class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
    @Override
    public void configure(HttpSecurity http) throws Exception {

      AuthenticationManager authenticationManager =
          http.getSharedObject(AuthenticationManager.class);

      InitialAuthenticationFilter initialAuthenticationFilter = new InitialAuthenticationFilter();

      JwtAuthenticationFilter jwtAuthenticationFilter =
          new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider, redisTemplate);

      http.addFilterBefore(initialAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
          .addFilterAfter(jwtAuthenticationFilter, InitialAuthenticationFilter.class)
          .authenticationProvider(jwtAuthenticationProvider);
    }
  }
}
