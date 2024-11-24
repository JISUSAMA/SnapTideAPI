package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@Builder
@AllArgsConstructor
public class PageRequestDTO {
  private int page; // 요청되는 page 번호
  private int size; // 페이지당 목록 갯수
  private String type;
  private String keyword;

  // 첫 페이지가 1페이지로 가기 때문
  public PageRequestDTO() {
    page = 1; size = 10;
  }

  // Pageable은 페이지 처리를 위한 객체
  public Pageable getPageable(Sort sort) {
    // Pageable 객체는 페이지 번호가 0부터 시작
    return PageRequest.of(page - 1, size, sort);
  }
}
