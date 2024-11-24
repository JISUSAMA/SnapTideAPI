package com.example.api.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// CORS(Cross Origin Resource Sharing)
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CORSFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String origin = request.getHeader("Origin"); // 요청에서 Origin 값을 읽어옴
    if (origin != null && (origin.equals("http://localhost:3000") || origin.equals("http://localhost:3001"))) {
      response.setHeader("Access-Control-Allow-Origin", origin); // 요청 Origin을 허용
    }

    response.setHeader("Access-Control-Allow-Credentials", "true"); // 인증 정보 허용
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // 허용 메서드
    response.setHeader("Access-Control-Max-Age", "3600"); // CORS 캐싱 시간
    response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization"); // 허용 헤더

    // OPTIONS 요청에 대한 응답 처리
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      response.setStatus(HttpServletResponse.SC_OK); // 응답 상태 OK
      return; // 필터 체인 중단
    }

    // 다음 필터로 요청 전달
    filterChain.doFilter(request, response);
  }
}
