package com.jeontongju.authentication.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.jeontongju.authentication.security.CustomAuthenticationProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsFilter corsFilter;
    private final CustomAuthenticationProvider customAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf()
            .disable()
            .sessionManagement(
                sessionManagement ->
                    sessionManagement.sessionCreationPolicy(STATELESS)
            )
            .addFilter(corsFilter)
            .httpBasic()
            .disable()
            .formLogin()
            .disable()
            .authenticationProvider(customAuthenticationProvider)
            .authorizeRequests(
                authz ->
                    authz
                        .antMatchers("/api/sign-up/**").permitAll()
                        .antMatchers("/api/sign-in/**").permitAll()
                        .anyRequest().authenticated()
            );
        return http.build();
    }
}
