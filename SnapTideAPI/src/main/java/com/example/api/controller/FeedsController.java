package com.example.api.controller;

import com.example.api.dto.FeedsDTO;
import com.example.api.dto.PageRequestDTO;
import com.example.api.dto.PageResultDTO;
import com.example.api.dto.PhotosDTO;
import com.example.api.entity.Feeds;
import com.example.api.service.FeedsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

  // 피드 목록 조회
  @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> list(PageRequestDTO pageRequestDTO) {
    log.info("PageRequestDTO: {}", pageRequestDTO);
    Map<String, Object> result = new HashMap<>();
    result.put("pageResultDTO", feedsService.getList(pageRequestDTO));
    result.put("pageRequestDTO", pageRequestDTO);
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // 피드 상세 조회
  @GetMapping(value = "/read/{fno}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<FeedsDTO> read(@PathVariable Long fno) {
    log.info("Fetching Feed with FNO: {}", fno);
    FeedsDTO feedsDTO = feedsService.getFeeds(fno);

    if (feedsDTO == null) {
      log.error("Feed not found for FNO: {}", fno);
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(feedsDTO, HttpStatus.OK);
  }
  @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Long> registerFeed(@RequestBody FeedsDTO feedsDTO) {
    log.info(">>"+feedsDTO);
    Long fno = feedsService.register(feedsDTO);
    return new ResponseEntity<>(fno, HttpStatus.OK);
  }

  @PostMapping(value = "/modify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<String> modify(
      @RequestPart("feed") FeedsDTO feedsDTO,
      @RequestPart(value = "images", required = false) List<MultipartFile> images,
      @RequestParam(value = "deletedImages", required = false) List<String> deletedImages) {

    log.info("Modifying Feed: {}", feedsDTO);

    try {
      feedsService.modifyWithFiles(feedsDTO, images, deletedImages);
      return ResponseEntity.ok("Feed modified successfully");
    } catch (Exception e) {
      log.error("Failed to modify feed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Feed modification failed");
    }
  }



  // 피드 삭제
  @DeleteMapping("/{fno}")
  public ResponseEntity<Map<String, String>> remove(@PathVariable Long fno) {
    log.info("Removing Feed with FNO: {}", fno);

    List<String> removedFiles = feedsService.removeWithReviewsAndPhotos(fno);
    removedFiles.forEach(file -> log.info("Removed file: {}", file));

    Map<String, String> result = new HashMap<>();
    result.put("message", fno + " 삭제 완료");
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // 파일 삭제
  @PostMapping("/remove")
  public String removeFile(Long fno, RedirectAttributes ra, PageRequestDTO pageRequestDTO) {
    log.info("remove post... fno: {}", fno);

    List<String> result = feedsService.removeWithReviewsAndPhotos(fno);
    result.forEach(fileName -> {
      try {
        String srcFileName = URLDecoder.decode(fileName, "UTF-8");
        File file = new File(uploadPath + File.separator + srcFileName);
        file.delete();
        File thumb = new File(file.getParent(), "s_" + file.getName());
        thumb.delete();
      } catch (Exception e) {
        log.error("Error removing file: {}", e.getMessage());
      }
    });

    if (feedsService.getList(pageRequestDTO).getDtoList().isEmpty() && pageRequestDTO.getPage() > 1) {
      pageRequestDTO.setPage(pageRequestDTO.getPage() - 1);
    }
    typeKeywordInit(pageRequestDTO);

    ra.addFlashAttribute("msg", fno + " 삭제");
    ra.addAttribute("page", pageRequestDTO.getPage());
    ra.addAttribute("type", pageRequestDTO.getType());
    ra.addAttribute("keyword", pageRequestDTO.getKeyword());
    return "redirect:/movies/list";
  }

  // 헬퍼 메서드: 페이지 요청 초기화
  private void typeKeywordInit(PageRequestDTO pageRequestDTO) {
    if (pageRequestDTO.getType() == null || pageRequestDTO.getType().equals("null")) {
      pageRequestDTO.setType("");
    }
    if (pageRequestDTO.getKeyword() == null || pageRequestDTO.getKeyword().equals("null")) {
      pageRequestDTO.setKeyword("");
    }
  }
}
