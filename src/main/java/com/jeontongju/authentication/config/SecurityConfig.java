package com.jeontongju.authentication.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.jeontongju.authentication.security.jwt.JwtAuthenticationProvider;
import com.jeontongju.authentication.security.jwt.JwtTokenProvider;
import com.jeontongju.authentication.security.jwt.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
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
  private final OAuth2UserService oAuth2UserService;

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

    http.oauth2Login(
        oauth2Login ->
            oauth2Login
                .loginPage("/api/sign-in/oauth")
                .userInfoEndpoint(
                    userInfoEndpointConfig ->
                        userInfoEndpointConfig.userService(oAuth2UserService)));

    http.authorizeRequests(
        authz ->
            authz
                .antMatchers("/api/**/sign-up/**")
                .permitAll()
                .antMatchers("/api/sign-in/**")
                .permitAll()
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
          new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider);

      // UsernamePasswordAuthenticationFilter 직전
      http.addFilterAfter(jwtAuthenticationFilter, LogoutFilter.class)
          .authenticationProvider(jwtAuthenticationProvider);
    }
  }
}
