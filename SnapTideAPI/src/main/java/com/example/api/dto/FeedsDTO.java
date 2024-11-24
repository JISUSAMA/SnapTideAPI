package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedsDTO {
  private Long fno;
  private String title;
  @Builder.Default // @AllArgsConstructor가 없으면 에러,기본값초기화
  private List<PhotosDTO> photosDTOList = new ArrayList<>();
  private double likes;
  private Long reviewsCnt;
  private LocalDateTime regDate;
  private LocalDateTime modDate;
}
