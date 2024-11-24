package com.example.api.repository;

import com.example.api.entity.Feeds;
import com.example.api.entity.Photos;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@SpringBootTest
class FeedsRepositoryTests {
  @Autowired
  FeedsRepository feedsRepository;

  @Autowired
  PhotosRepository photosRepository;

  @Transactional
  @Commit
  @Test
  public void insertFeeds() {
    IntStream.rangeClosed(1, 100).forEach(i -> {
      Feeds feeds = Feeds.builder().title("Movie..." + i).build();
      feedsRepository.save(feeds);
      System.out.println("----------------------------");
      int cnt = (int) (Math.random() * 5) + 1;
      for (int j = 0; j < cnt; j++) {
        Photos photos = Photos.builder()
            .uuid(UUID.randomUUID().toString())
            .feeds(feeds)
            .photosName("test" + j + ".jpg")
            .build();
        photosRepository.save(photos);
      }
    });
  }

  @Test
  public void testListPage() {
    PageRequest pageRequest =
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "mno"));
    Page<Object[]> result = feedsRepository.getListPage(pageRequest);
    for (Object[] objArr : result.getContent()) {
      System.out.println(Arrays.toString(objArr));
    }
  }
  @Test
  public void testListPageImg() {
    PageRequest pageRequest =
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "mno"));
    Page<Object[]> result = feedsRepository.getListPageImg(pageRequest);
    for (Object[] objArr : result.getContent()) {
      System.out.println(Arrays.toString(objArr));
    }
  }
  @Test
  public void testGetListPageImgNative() {
    PageRequest pageRequest =
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "movie_mno"));
    Page<Object[]> result = feedsRepository.getListPageImgNative(pageRequest);
    for (Object[] objArr : result.getContent()) {
      System.out.println(Arrays.toString(objArr));
    }
  }

  @Test
  public void testGetListPageImgJPQL() {
    PageRequest pageRequest =
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "mno"));
    Page<Object[]> result = feedsRepository.getListPageImgJPQL(pageRequest);
    for (Object[] objArr : result.getContent()) {
      System.out.println(Arrays.toString(objArr));
    }
  }

  @Test
  public void testGetMaxQuery() {
    PageRequest pageRequest =
        PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "movie"));
    Page<Object[]> result = feedsRepository.getMaxQuery(pageRequest);
    for (Object[] objArr : result.getContent()) {
      System.out.println(Arrays.toString(objArr));
    }
  }

  @Test
  public void testGetMovieWithAll() {
    List<Object[]> result = feedsRepository.getFeedsWithAll(100L);
    for (Object[] arr : result) {
      System.out.println(Arrays.toString(arr));
    }
  }

//  @Test
//  public void testSearch1() {
//    movieRepository.search1();
//  }

  @Test
  public void testSearchPage() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("mno").descending().and(Sort.by("title").ascending()));
    Page<Object[]> result = feedsRepository.searchPage("t", "1", pageable);
  }
}
