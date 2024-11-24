package com.example.api.controller;

import com.example.api.dto.UploadResultDTO;
import com.example.api.repository.PhotosRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@Log4j2
@RequiredArgsConstructor
public class UploadController {
  private final PhotosRepository photosRepository;

  @Value("${com.example.upload.path}")
  private String uploadPath;

  @PostMapping("/uploadAjax")
  public ResponseEntity<List<UploadResultDTO>> upload(MultipartFile[] uploadFiles) {
    List<UploadResultDTO> resultDTOList = new ArrayList<>();

    for (MultipartFile multipartFile : uploadFiles) {
      String originalName = multipartFile.getOriginalFilename();
      String fileName = originalName.substring(originalName.lastIndexOf("\\") + 1);
      log.info("fileName: " + fileName);

      String folderPath = makeFolder(); // yyyy/MM/dd
      String uuid = UUID.randomUUID().toString(); //unique
      String saveName = uploadPath + File.separator + folderPath + File.separator
          + uuid + "_" + fileName;
      Path savePath = Paths.get(saveName);
      try {
        multipartFile.transferTo(savePath); //원본 파일 저장
        String thumbnailSaveName = uploadPath + File.separator + folderPath
            + File.separator + "s_" + uuid + "_" + fileName;
        File thumbnailFile = new File(thumbnailSaveName);
        Thumbnailator.createThumbnail(savePath.toFile(), thumbnailFile, 100, 100);
        resultDTOList.add(new UploadResultDTO(fileName, uuid, folderPath));
      } catch (IOException e) {
        log.error(e.getMessage());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    return new ResponseEntity<>(resultDTOList, HttpStatus.OK);
  }

  @GetMapping("/display")
  public ResponseEntity<byte[]> getImageFile(String fileName, String size) {
    ResponseEntity<byte[]> result = null;
    try {
      String searchFilename = URLDecoder.decode(fileName, "UTF-8");
      File file = new File(uploadPath + File.separator + searchFilename);
      if (size != null && size.equals("1")) {
        log.info(">>", file.getName());
        // 미리보기 할 때 링크에 size=1로 설정하여 섬네일명에서 s_ 를 제거하고 가져옴
        file = new File(file.getParent(), file.getName().substring(2));
      }
      log.info("file: " + file);
      HttpHeaders headers = new HttpHeaders();
      // 파일의 확장자에 따라서 브라우저에 전송하는 MIME타입을 결정
      headers.add("Content-Type", Files.probeContentType(file.toPath()));
      result = new ResponseEntity<>(
          FileCopyUtils.copyToByteArray(file), headers, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    return result;
  }

  @Transactional
  @PostMapping("/removeFile")
  public ResponseEntity<Boolean> removeFile(String fileName, String uuid) {
    log.info("remove fileName: " + fileName);
    String searchFilename = null;
    if (uuid != null) {
      photosRepository.deleteByUuid(uuid);
    }
    try {
      searchFilename = URLDecoder.decode(fileName, "UTF-8");
      File file = new File(uploadPath + File.separator + searchFilename);
      boolean result1 = file.delete();
      File thumbnail = new File(file.getParent(), "s_" + file.getName());
      boolean result2 = thumbnail.delete();
      boolean tmp = result1 && result2;
      log.info(">>", tmp + "=" + result1 + "&&" + result2);
      return new ResponseEntity<>(tmp,
          tmp ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      log.error(e.getMessage());
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String makeFolder() {
    String str = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
    String folderPath = str.replace("/", File.separator);
    File uploadPathFolder = new File(uploadPath, folderPath);
    if (!uploadPathFolder.exists()) uploadPathFolder.mkdirs();
    return folderPath;
  }
}
