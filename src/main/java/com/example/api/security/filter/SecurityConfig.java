import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors().and() // CORS 설정 활성화
        .csrf().disable() // CSRF 비활성화
        .authorizeRequests()
        .requestMatchers("/api/**").permitAll() // API 요청은 인증 없이 허용
        .anyRequest().authenticated(); // 나머지 요청은 인증 필요

    return http.build();
  }
}