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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
  public ApiLoginFilter apiLoginFilter(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    ApiLoginFilter apiLoginFilter = new ApiLoginFilter("/auth/login", jwtUtil());
    apiLoginFilter.setAuthenticationManager(
        authenticationConfiguration.getAuthenticationManager());
    apiLoginFilter.setAuthenticationFailureHandler(new ApiLoginFailHandler());
    return apiLoginFilter;
  }

  @Bean   //자체적으로 AuthenticationManager을 생성하기 위한 Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration conf) throws Exception {
    return conf.getAuthenticationManager();
  }

  // String 배열에 정의된 주소는 token으로 인증해야만 접근할 수 있는 주소.
  String[] checkAddress = {"/feeds/**", "/members/**", "/reviews/**", "/reviews/all/**"
//      , "/uploadAjax/**", "/removeFile/**"
//      ,"/display/**"
  };

  @Bean
  public ApiCheckFilter apiCheckFilter() {
    return new ApiCheckFilter(checkAddress, jwtUtil());
  }

  @Bean
  protected SecurityFilterChain config(HttpSecurity httpSecurity)
      throws Exception {
    // csrf 사용안하는 설정 Cross-Site Request Forgery
    httpSecurity.csrf(httpSecurityCsrfConfigurer -> {
      httpSecurityCsrfConfigurer.disable();
    });

    httpSecurity.authorizeHttpRequests(
        auth -> auth
            .requestMatchers(new AntPathRequestMatcher("/feeds/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/members/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/reviews/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/reviews/all/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/uploadAjax/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/display/**")).permitAll()
            .requestMatchers(new AntPathRequestMatcher("/removeFile/**")).permitAll()
            .anyRequest().denyAll());

    // addFilterBefore는 일반적 필터링 순서보다 앞쪽에서 필터링하도록 순서 조정.
    httpSecurity.addFilterBefore(
        apiCheckFilter(), UsernamePasswordAuthenticationFilter.class);
    // BasicAuthenticationFilter.class 도 사용가능

    httpSecurity.addFilterBefore(
        apiLoginFilter(httpSecurity.getSharedObject(AuthenticationConfiguration.class)),
        UsernamePasswordAuthenticationFilter.class
    );
    return httpSecurity.build();
  }

}
