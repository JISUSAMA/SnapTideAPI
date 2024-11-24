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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLDecoder;
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

  private void deleteFile(String fileName) {
    // 실제 파일도 지우기
    String searchFilename = null;
    try {
      searchFilename = URLDecoder.decode(fileName, "UTF-8");
      File file = new File(uploadPath + File.separator + searchFilename);
      file.delete();
      new File(file.getParent(), "s_" + file.getName()).delete();
    } catch (Exception e) {
      log.error(e.getMessage());
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
