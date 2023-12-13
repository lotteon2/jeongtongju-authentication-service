package com.jeontongju.authentication.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOrigins(
        List.of(
            "https://jeontongju-jumo.netlify.app/",
            "https://test-jeontongju-jumo.netlify.app/",
            "https://jeontongju-front-admin.vercel.app/",
            "https://jeontongju-front-consumer.vercel.app/"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.addAllowedHeader("*");
    config.addExposedHeader("Authorization");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}
