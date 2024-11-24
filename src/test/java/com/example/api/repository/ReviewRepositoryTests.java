package com.example.api.repository;

import com.example.api.entity.Feeds;
import com.example.api.entity.Members;
import com.example.api.entity.Reviews;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
class ReviewRepositoryTests {
  @Autowired
  ReviewsRepository reviewsRepository;
  @Autowired
  MembersRepository membersRepository;

  @Test
  public void insertReviews() {
    IntStream.rangeClosed(1, 200).forEach(i -> {
      Long fno = (long) (Math.random() * 100) + 1;
      Long mid = (long) (Math.random() * 100) + 1;
      Reviews reviews = Reviews.builder()
          .members(Members.builder().mid(mid).build())
          .feeds(Feeds.builder().fno(fno).build())
          .likes((int) (Math.random() * 5) + 1)
          .text("이 영화는.....")
          .build();
      reviewsRepository.save(reviews);
    });
  }

  @Test
  public void testFindByMovie() {
    List<Reviews> result = reviewsRepository.findByFeeds(
        Feeds.builder().fno(100L).build()
    );
    result.forEach(reviews -> {
      System.out.println(reviews.getReviewsnum());
      System.out.println(reviews.getLikes());
      System.out.println(reviews.getText());
      System.out.println(reviews.getMembers().getEmail());
      System.out.println();
    });
  }

  @Test
  @Transactional
  @Commit
  public void testDeleteByMember() {
    Long mid = 1L;
    Members members = Members.builder().mid(mid).build();
    reviewsRepository.deleteByMembers(members);
    membersRepository.deleteById(mid);
  }
}