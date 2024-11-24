package com.example.api.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;


@ControllerAdvice
public class ControllerExceptionAdvice {
  // controller와 같이 동작하는 개념, 마치 필터같은 개념
  // controller를 별도로 만들지 않아도 되며, SecurityConfig에 permitAll만 처리.

  // 404 exception
  @ExceptionHandler(NoResourceFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handl404(NoResourceFoundException e, Model model) {
    model.addAttribute("errorMessage", e.getMessage());
    return "/error/notFound";
  }

  // Database error
  @ExceptionHandler(DataAccessException.class)
  public String handleDataAccessException(DataAccessException e, Model model) {
    model.addAttribute("errorMessage", e.getMessage());
    e.printStackTrace();
    return "error/databaseError";
  }

  // internal server error
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public String handleException(Exception e, Model model) {
    model.addAttribute("errorMessage", e.getMessage());
    e.printStackTrace();
    return "error/serverError";
  }
}
