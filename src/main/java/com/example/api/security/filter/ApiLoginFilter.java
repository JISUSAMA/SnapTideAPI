package com.example.api.security.filter;

import com.example.api.security.dto.ClubMemberAuthDTO;
import com.example.api.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import java.io.IOException;

@Log4j2
public class ApiLoginFilter extends AbstractAuthenticationProcessingFilter {
  private JWTUtil jwtUtil;

  public ApiLoginFilter(String defaultFilterProcessUrl, JWTUtil jwtUtil) {
    super(defaultFilterProcessUrl);
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException, IOException, ServletException {
    String email = request.getParameter("email");
    String pass = request.getParameter("pw");
//    log.info("attemptAuthentication...."+email+"/"+pass);
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(email, pass);
    return getAuthenticationManager().authenticate(authToken);
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authResult)
      throws IOException, ServletException {
    log.info("successfulAuthentication :: authResult " + authResult.getPrincipal());
    String email = ((ClubMemberAuthDTO) authResult.getPrincipal()).getUsername();
    String token = null;
    try {
      token = jwtUtil.generateToken(email);
      response.setContentType("text/plain");
      response.getOutputStream().write(token.getBytes());
      log.info("generated token :" + token);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
