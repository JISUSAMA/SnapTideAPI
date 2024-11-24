package com.example.api.security.filter;

import com.example.api.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import net.minidev.json.JSONObject;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Log4j2
public class ApiCheckFilter extends OncePerRequestFilter {
  //ApiCheckFilter의 기능 2가지
  //1) 요청된 주소와 패턴(허락된 rest주소)와 일치하는지 비교
  //2) 일치하면 주소에 토큰의 유무를 확인

  // 주소의 패턴
  private String[] pattern;
  // 요청되는 주소와 패턴의 주소를 비교해주는 객체
  private AntPathMatcher antPathMatcher;
  private JWTUtil jwtUtil;

  public ApiCheckFilter(String[] pattern, JWTUtil jwtUtil) {
    antPathMatcher = new AntPathMatcher();
    this.pattern = pattern;
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    log.info("request.getRequestURI()" + request.getRequestURI());
    log.info("request.getContextPath()" + request.getContextPath());
    boolean check = false;
    for (int i = 0; i < pattern.length; i++) {
      log.info("REQUEST match: >> " + request.getContextPath() + pattern[i]
          + "/" + request.getRequestURI());
      // 요청된 주소와 패턴주소가 일치할 경우에 조정한다.
      if (antPathMatcher.match(request.getContextPath() + pattern[i],
          request.getRequestURI())) {
        check = true;
        break;
      }
    }
    // 요청주소와 패턴이 일치할 경우 분기
    if (check) {
      log.info("check : " + check);

      // 토큰 유무에 대하여 checkAuthHeader를 통해 확인
      boolean checkTokenHeader = checkAuthHeader(request);

      // checkAuthHeader는 token 유무에 의한 분기
      if (checkTokenHeader) {
        filterChain.doFilter(request, response);
        return;
      } else {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        String message = "FAIL CHECK API TOKEN";
        jsonObject.put("code", "403");
        jsonObject.put("message", message);
        PrintWriter printWriter = response.getWriter();
        printWriter.println(jsonObject);
        return;
      }
    }
    filterChain.doFilter(request, response);// 요청주소와 패턴주소 불일치
  }

  private boolean checkAuthHeader(HttpServletRequest request) {
    boolean checkResult = false;
    String authHeader = request.getHeader("Authorization");
    if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
      log.info("Authorization : " + authHeader);
      try {
        String email = jwtUtil.validateAndExtract(authHeader.substring(7));
        log.info("validate result: " + email);
        checkResult = email.length() > 0;
      } catch (Exception e) {e.printStackTrace();}
    }
    return checkResult;
  }
}
