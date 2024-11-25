package com.example.api.config;

import com.example.api.security.filter.ApiCheckFilter;
import com.example.api.security.filter.ApiLoginFilter;
import com.example.api.security.handler.ApiLoginFailHandler;
import com.example.api.security.util.JWTUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JWTUtil jwtUtil() {
    return new JWTUtil();
  }

  @Bean
  public ApiLoginFilter apiLoginFilter(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    ApiLoginFilter apiLoginFilter = new ApiLoginFilter("/auth/login", jwtUtil());
    apiLoginFilter.setAuthenticationManager(authenticationConfiguration.getAuthenticationManager());
    apiLoginFilter.setAuthenticationFailureHandler(new ApiLoginFailHandler());
    return apiLoginFilter;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration conf) throws Exception {
    return conf.getAuthenticationManager();
  }

  String[] checkAddress = {"/feeds/**", "/members/**", "/reviews/**", "/reviews/all/**"};

  @Bean
  public ApiCheckFilter apiCheckFilter() {
    return new ApiCheckFilter(checkAddress, jwtUtil());
  }

  @Bean
  protected SecurityFilterChain config(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf(csrf -> csrf.disable()) // CSRF 비활성화
        .cors().and() // CORS 활성화
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/login").permitAll()
            .requestMatchers("/feeds/**").permitAll()
            .requestMatchers("/members/**").permitAll()
            .requestMatchers("/reviews/**").permitAll()
            .requestMatchers("/uploadAjax").permitAll() // 파일 업로드 허용
            .requestMatchers("/removeFile").permitAll() // 파일 삭제 허용
            .requestMatchers("/display").permitAll() // 이미지 표시 허용
            .anyRequest().authenticated());

    httpSecurity.addFilterBefore(apiCheckFilter(), UsernamePasswordAuthenticationFilter.class);
    httpSecurity.addFilterBefore(apiLoginFilter(httpSecurity.getSharedObject(AuthenticationConfiguration.class)),
        UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:3000"); // React 포트 3000
    configuration.addAllowedOrigin("http://localhost:3001"); // React 포트 3001 추가
    configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
    configuration.addAllowedHeader("*"); // 모든 헤더 허용
    configuration.setAllowCredentials(true); // 인증 정보 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
