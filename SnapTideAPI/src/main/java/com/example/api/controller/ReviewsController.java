package com.example.api.controller;

import com.example.api.dto.ReviewsDTO;
import com.example.api.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewsController {
  private final ReviewService reviewService;

  @GetMapping(value = "/all/{fno}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ReviewsDTO>> getList(@PathVariable("fno") Long fno) {
    log.info("fno: " + fno);
    List<ReviewsDTO> reviewsDTOList = reviewService.getListOfFeeds(fno);
    return new ResponseEntity<>(reviewsDTOList, HttpStatus.OK);
  }

  @PostMapping("/{fno}")
  // @RequestBody : form이나, json 데이터를 전송받을 때
  // @RequestParam : 변수로 데이터를 전송받을 때
  public ResponseEntity<Long> register(@RequestBody ReviewsDTO reviewsDTO) {
    log.info(">>" + reviewsDTO);
    Long reviewsnum = reviewService.register(reviewsDTO);
    return new ResponseEntity<>(reviewsnum, HttpStatus.OK);
  }

  @PutMapping("/{fno}/{reviewsnum}")
  public ResponseEntity<Long> modify(@RequestBody ReviewsDTO reviewsDTO) {
    log.info(">>" + reviewsDTO);
    reviewService.modify(reviewsDTO);
    return new ResponseEntity<>(reviewsDTO.getReviewsnum(), HttpStatus.OK);
  }

  @DeleteMapping("/{fno}/{reviewsnum}")
  public ResponseEntity<Long> delete(@PathVariable Long reviewsnum) {
    log.info(">>" + reviewsnum);
    reviewService.remove(reviewsnum);
    return new ResponseEntity<>(reviewsnum, HttpStatus.OK);
  }

}
