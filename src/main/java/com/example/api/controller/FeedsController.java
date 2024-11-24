package com.example.api.controller;

import com.example.api.dto.FeedsDTO;
import com.example.api.dto.PageRequestDTO;
import com.example.api.dto.PageResultDTO;
import com.example.api.dto.PhotosDTO;
import com.example.api.service.FeedsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log4j2
@RequestMapping("/feeds")
@RequiredArgsConstructor
public class FeedsController {
  private final FeedsService feedsService;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  private void typeKeywordInit(PageRequestDTO pageRequestDTO) {
    if (pageRequestDTO.getType() == null || pageRequestDTO.getType().equals("null")) {
      pageRequestDTO.setType("");
    }
    if (pageRequestDTO.getKeyword() == null || pageRequestDTO.getKeyword().equals("null")) {
      pageRequestDTO.setKeyword("");
    }
  }

  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> list(PageRequestDTO pageRequestDTO) {
    log.info("PageRequestDTO: " + pageRequestDTO);
    Map<String, Object> result = new HashMap<>();
    result.put("pageResultDTO", feedsService.getList(pageRequestDTO));
    result.put("pageRequestDTO", pageRequestDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Long> registerFeed(@RequestBody FeedsDTO feedsDTO) {
    log.info("Registering Feed: " + feedsDTO);
    Long fno = feedsService.register(feedsDTO);
    return new ResponseEntity<>(fno, HttpStatus.OK);
  }

  @GetMapping(value = "/read/{fno}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> getFeed(@PathVariable("fno") Long fno) {
    log.info("Fetching Feed with FNO: " + fno);
    FeedsDTO feedsDTO = feedsService.getFeeds(fno);

    if (feedsDTO == null) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", "Feed not found");
      return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("feedsDTO", feedsDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, String>> modify(@RequestBody FeedsDTO dto,
                                                    @RequestParam Map<String, String> params) {
    log.info("Modifying Feed: " + dto);
    feedsService.modify(dto);

    PageRequestDTO pageRequestDTO = new PageRequestDTO();
    pageRequestDTO.setPage(Integer.parseInt(params.getOrDefault("page", "1")));
    pageRequestDTO.setType(params.getOrDefault("type", ""));
    pageRequestDTO.setKeyword(params.getOrDefault("keyword", ""));
    typeKeywordInit(pageRequestDTO);

    Map<String, String> result = new HashMap<>();
    result.put("msg", dto.getFno() + " 수정");
    result.put("fno", dto.getFno() + "");
    result.put("page", pageRequestDTO.getPage() + "");
    result.put("type", pageRequestDTO.getType());
    result.put("keyword", pageRequestDTO.getKeyword());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/remove/{fno}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, String>> remove(
      @PathVariable Long fno, @RequestBody(required = false) PageRequestDTO pageRequestDTO) {

    if (pageRequestDTO == null) {
      pageRequestDTO = new PageRequestDTO();
      pageRequestDTO.setPage(1);
      pageRequestDTO.setType("");
      pageRequestDTO.setKeyword("");
    }

    log.info("Removing Feed with FNO: " + fno);
    List<String> photoList = feedsService.removeWithReviewsAndPhotos(fno);
    photoList.forEach(fileName -> {
      try {
        log.info("Removing File: " + fileName);
        String srcFileName = URLDecoder.decode(fileName, "UTF-8");
        File file = new File(uploadPath + File.separator + srcFileName);
        if (file.exists()) file.delete();
        File thumb = new File(file.getParent(), "s_" + file.getName());
        if (thumb.exists()) thumb.delete();
      } catch (Exception e) {
        log.error("Error removing file: " + e.getMessage());
      }
    });

    if (feedsService.getList(pageRequestDTO).getDtoList().isEmpty() && pageRequestDTO.getPage() > 1) {
      pageRequestDTO.setPage(pageRequestDTO.getPage() - 1);
    }
    typeKeywordInit(pageRequestDTO);

    Map<String, String> result = new HashMap<>();
    result.put("msg", fno + " 삭제");
    result.put("page", pageRequestDTO.getPage() + "");
    result.put("type", pageRequestDTO.getType());
    result.put("keyword", pageRequestDTO.getKeyword());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
