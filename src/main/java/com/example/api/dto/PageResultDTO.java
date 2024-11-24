package com.example.api.dto;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class PageResultDTO<DTO, EN> {
  private List<DTO> dtoList; // 한 페이지의 목록
  private int totalPage; //총페이지수 10개
  private int page; // 현재 페이지
  private int size; // 한 페이지의 목록수
  private int start, end; //시작 페이지와 끝페이지 번호
  private boolean prev, next; // 다음 페이지와 이전 페이지 유무
  private List<Integer> pageList; // 페이지 번호에 대한 목록

  // 생성자를 통해서 페이징된 result의 각각의 원소에 대한 함수 적용
  public PageResultDTO(Page<EN> result, Function<EN, DTO> fn) {
    // Page를 List로 변경 처리
    dtoList = result.stream().map(fn).collect(Collectors.toList());
    // 전체 페이지 개수 구하기
    totalPage = result.getTotalPages();
    Pageable pageable = result.getPageable();
    page = pageable.getPageNumber() + 1; //0부터 시작함
    size = pageable.getPageSize();

    int tempEnd = (int) Math.ceil((page/10.0)) * 10;
    start = tempEnd - 9;
    prev = start > 1;
    end = totalPage > tempEnd ? tempEnd : totalPage;
    next = totalPage > tempEnd;
    pageList = //페이지 번호 목록
        IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
  }
}
