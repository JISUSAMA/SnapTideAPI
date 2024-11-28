package com.example.api.service;

import com.example.api.dto.FeedsDTO;
import com.example.api.dto.PageRequestDTO;
import com.example.api.dto.PageResultDTO;
import com.example.api.entity.Feeds;
import com.example.api.entity.Photos;
import com.example.api.repository.FeedsRepository;
import com.example.api.repository.PhotosRepository;
import com.example.api.repository.ReviewsRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLDecoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Log4j2
@RequiredArgsConstructor
public class FeedsServiceImpl implements FeedsService {
  private final FeedsRepository feedsRepository;
  private final PhotosRepository photosRepository;
  private final ReviewsRepository reviewsRepository;

  @Override
  public Long register(FeedsDTO feedsDTO) {
    Map<String, Object> entityMap = dtoToEntity(feedsDTO);
    Feeds feeds = (Feeds) entityMap.get("feeds");
    List<Photos> photosList = (List<Photos>) entityMap.get("photosList");
    feedsRepository.save(feeds);
    if (photosList != null) {
      photosList.forEach(new Consumer<Photos>() {
        @Override
        public void accept(Photos photos) {
          photosRepository.save(photos);
        }
      });
    }
    return feeds.getFno();
  }
  @Override
  public PageResultDTO<FeedsDTO, Object[]> getList(PageRequestDTO pageRequestDTO) {
    Pageable pageable = pageRequestDTO.getPageable(Sort.by("fno").descending());
    // Page<Feeds> result = feedsRepository.findAll(pageable);
//    Page<Object[]> result = feedsRepository.getListPageImg(pageable);
    Page<Object[]> result = feedsRepository.searchPage(pageRequestDTO.getType(),
        pageRequestDTO.getKeyword(),
        pageable);
    Function<Object[], FeedsDTO> fn = objects -> entityToDto(
        (Feeds) objects[0],
        (List<Photos>) (Arrays.asList((Photos) objects[1])),
        (Long) objects[2],
        (Long) objects[3]
    );
    return new PageResultDTO<>(result, fn);
  }

  @Override
  public FeedsDTO getFeeds(Long fno) {
    List<Object[]> result = feedsRepository.getFeedsWithAll(fno);
    Feeds feeds = (Feeds) result.get(0)[0];
    List<Photos> photos = new ArrayList<>();
    result.forEach(objects -> photos.add((Photos) objects[1]));
    Long likes = (Long) result.get(0)[2];
    Long reviewsCnt = (Long) result.get(0)[3];

    return entityToDto(feeds, photos, likes, reviewsCnt);
  }

  @Value("${com.example.upload.path}")
  private String uploadPath;

  @Transactional
  @Override
  public void modify(FeedsDTO feedsDTO) {
    Optional<Feeds> result = feedsRepository.findById(feedsDTO.getFno());
    if (result.isPresent()) {
      Map<String, Object> entityMap = dtoToEntity(feedsDTO);
      Feeds feeds = (Feeds) entityMap.get("feeds");
      feeds.changeTitle(feedsDTO.getTitle());
      feedsRepository.save(feeds);
      // photosList :: 수정창에서 이미지 수정할 게 있는 경우의 목록
      List<Photos> newPhotosList =
          (List<Photos>) entityMap.get("photosList");

      List<Photos> oldPhotosList =
          photosRepository.findByMno(feeds.getFno());
      if (newPhotosList == null) {
        // 수정창에서 이미지 모두를 지웠을 때
        photosRepository.deleteByFno(feeds.getFno());
        for (int i = 0; i < oldPhotosList.size(); i++) {
          Photos oldPhotos = oldPhotosList.get(i);
          String fileName = oldPhotos.getPath() + File.separator
              + oldPhotos.getUuid() + "_" + oldPhotos.getPhotosName();
          deleteFile(fileName);
        }
      } else { // newFeedsImageList에 일부 변화 발생
        newPhotosList.forEach(photos -> {
          boolean result1 = false;
          for (int i = 0; i < oldPhotosList.size(); i++) {
            result1 = oldPhotosList.get(i).getUuid().equals(photos.getUuid());
            if (result1) break;
          }
          if (!result1) photosRepository.save(photos);
        });
        oldPhotosList.forEach(oldPhotos -> {
          boolean result1 = false;
          for (int i = 0; i < newPhotosList.size(); i++) {
            result1 = newPhotosList.get(i).getUuid().equals(oldPhotos.getUuid());
            if (result1) break;
          }
          if (!result1) {
            photosRepository.deleteByUuid(oldPhotos.getUuid());
            String fileName = oldPhotos.getPath() + File.separator
                + oldPhotos.getUuid() + "_" + oldPhotos.getPhotosName();
            deleteFile(fileName);
          }
        });
      }
    }
  }
  @Transactional
  @Override
  public void modifyWithFiles(FeedsDTO feedsDTO, List<MultipartFile> images, List<String> deletedImages) {
    Optional<Feeds> optionalFeeds = feedsRepository.findById(feedsDTO.getFno());
    if (!optionalFeeds.isPresent()) {
      throw new IllegalArgumentException("Feed not found for ID: " + feedsDTO.getFno());
    }

    Feeds feeds = optionalFeeds.get();
    feeds.changeTitle(feedsDTO.getTitle());
    feedsRepository.save(feeds);

    // 1. 삭제된 이미지 처리
    if (deletedImages != null) {
      deletedImages.forEach(uuid -> {
        Photos photo = photosRepository.findByUuid(uuid);
        if (photo != null) {
          // 원본 파일 경로
          String originalFilePath = photo.getPath() + File.separator + photo.getUuid() + "_" + photo.getPhotosName();

          // 썸네일 파일 경로
          String thumbnailFilePath = photo.getPath() + File.separator + "s_" + photo.getUuid() + "_" + photo.getPhotosName();

          // 파일 삭제
          deleteFile(originalFilePath);
          deleteFile(thumbnailFilePath);

          // DB에서 삭제
          photosRepository.deleteByUuid(uuid);
        }
      });
    }

    // 2. 새 이미지 저장
    if (images != null && !images.isEmpty()) {
      images.forEach(image -> {
        try {
          // 폴더 경로 생성
          String folderPath = makeFolder(); // "2024\11\28" 반환

          // 파일 이름 생성
          String uuid = UUID.randomUUID().toString();
          String fileName = uuid + "_" + image.getOriginalFilename();

          // 원본 파일 저장 경로
          String saveName = uploadPath + File.separator + folderPath + File.separator + fileName;
          File saveFile = new File(saveName);

          // 파일 저장
          image.transferTo(saveFile);

          // 썸네일 생성
          String thumbnailSaveName = uploadPath + File.separator + folderPath + File.separator + "s_" + fileName;
          File thumbnailFile = new File(thumbnailSaveName);
          Thumbnailator.createThumbnail(saveFile, thumbnailFile, 100, 100);

          // DB에 저장
          Photos photo = Photos.builder()
              .uuid(uuid)
              .photosName(image.getOriginalFilename()) // 원래 파일명 저장
              .path(folderPath) // 폴더 경로만 저장
              .feeds(feeds)
              .build();

          photosRepository.save(photo);
        } catch (Exception e) {
          log.error("Error saving image: {}", e.getMessage());
        }
      });
    }
  }



  private String saveFile(MultipartFile file) throws Exception {
    // 폴더 경로 생성 (예: yyyy\MM\dd)
    String folderPath = makeFolder(); // 폴더 경로 생성 (2024\11\28 형태)

    // 업로드 디렉터리 생성
    File uploadDir = new File(uploadPath + File.separator + folderPath);
    if (!uploadDir.exists()) {
      boolean dirCreated = uploadDir.mkdirs();
      if (!dirCreated) {
        throw new RuntimeException("Failed to create upload directory: " + uploadDir.getPath());
      }
      log.info("Upload directory created: {}", uploadDir.getPath());
    }

    // 파일 이름 생성
    String uuid = UUID.randomUUID().toString();
    String fileName = uuid + "_" + file.getOriginalFilename();

    // 전체 경로 생성
    String fullPath = uploadDir.getPath() + File.separator + fileName;
    File destinationFile = new File(fullPath);

    // 파일 저장
    file.transferTo(destinationFile);
    log.info("Saved file: {}", fullPath);

    // 폴더 경로만 반환
    return folderPath; // "2024\11\28"만 반환
  }


  private String makeFolder() {
    // yyyy\MM\dd 형식으로 폴더 경로 생성
    String str = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy\\MM\\dd"));
    String folderPath = str;

    // 디렉토리 생성
    File uploadPathFolder = new File(uploadPath, folderPath);
    if (!uploadPathFolder.exists()) {
      uploadPathFolder.mkdirs();
    }
    return folderPath;
  }

  private void deleteFile(String filePath) {
    try {
      File file = new File(uploadPath + File.separator + filePath);
      if (file.exists()) {
        file.delete();
        log.info("Deleted file: {}", filePath);
      }
    } catch (Exception e) {
      log.error("Error deleting file: {}", e.getMessage());
    }
  }

  @Transactional
  @Override
  public List<String> removeWithReviewsAndPhotos(Long fno) {
    List<Photos> list = photosRepository.findByMno(fno);
    List<String> result = new ArrayList<>();
    list.forEach(new Consumer<Photos>() {
      @Override
      public void accept(Photos t) {
        result.add(t.getPath() + File.separator + t.getUuid() + "_" + t.getPhotosName());
      }
    });
    photosRepository.deleteByFno(fno);
    reviewsRepository.deleteByFno(fno);
    feedsRepository.deleteById(fno);
    return result;
  }

  @Override
  public void removeUuid(String uuid) {
    log.info("deleteImage...... uuid: " + uuid);
    photosRepository.deleteByUuid(uuid);
  }
}
