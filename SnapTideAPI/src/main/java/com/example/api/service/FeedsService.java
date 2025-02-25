package com.example.api.service;

import com.example.api.dto.FeedsDTO;
import com.example.api.dto.PageRequestDTO;
import com.example.api.dto.PageResultDTO;
import com.example.api.dto.PhotosDTO;
import com.example.api.entity.Feeds;
import com.example.api.entity.Photos;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface FeedsService {
  Long register(FeedsDTO feedsDTO);

  PageResultDTO<FeedsDTO, Object[]> getList(PageRequestDTO pageRequestDTO);

  FeedsDTO getFeeds(Long fno);

  void modify(FeedsDTO feedsDTO);

  @Transactional
  void modifyWithFiles(FeedsDTO feedsDTO, List<MultipartFile> images, List<String> deletedImages);

  List<String> removeWithReviewsAndPhotos(Long fno);

  void removeUuid(String uuid);

  default Map<String, Object> dtoToEntity(FeedsDTO feedsDTO) {
    Map<String, Object> entityMap = new HashMap<>();
    Feeds feeds = Feeds.builder()
        .fno(feedsDTO.getFno())
        .title(feedsDTO.getTitle())
        .build();
    entityMap.put("feeds", feeds);

    List<PhotosDTO> photosDTOList = feedsDTO.getPhotosDTOList();
    if (photosDTOList != null && !photosDTOList.isEmpty()) {
      List<Photos> photosList = photosDTOList.stream()
          .map(photosDTO -> Photos.builder()
              .path(photosDTO.getPath()) // path 값이 저장된 파일의 경로와 일치해야 함
              .photosName(photosDTO.getPhotosName())
              .uuid(photosDTO.getUuid())
              .feeds(feeds)
              .build())
          .collect(Collectors.toList());
      entityMap.put("photosList", photosList);
    }
    return entityMap;
  }

  default FeedsDTO entityToDto(Feeds feeds, List<Photos> photosList
      , Long likes, Long reviewsCnt) {
    FeedsDTO feedsDTO = FeedsDTO.builder()
        .fno(feeds.getFno())
        .title(feeds.getTitle())
        .regDate(feeds.getRegDate())
        .modDate(feeds.getModDate())
        .build();
    List<PhotosDTO> photosDTOList = new ArrayList<>();
    if(photosList.toArray().length > 0 && photosList.toArray()[0] != null) {
      photosDTOList = photosList.stream().map(
          photos -> {
            PhotosDTO photosDTO = PhotosDTO.builder()
                .photosName(photos.getPhotosName())
                .path(photos.getPath())
                .uuid(photos.getUuid())
                .build();
            return photosDTO;
          }
      ).collect(Collectors.toList());
    }
    feedsDTO.setPhotosDTOList(photosDTOList);
    feedsDTO.setLikes(likes);
    feedsDTO.setReviewsCnt(reviewsCnt);
    return feedsDTO;
  }
}
