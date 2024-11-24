package com.example.api.repository;

import com.example.api.entity.Feeds;
import com.example.api.repository.search.SearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedsRepository extends JpaRepository<Feeds, Long>, SearchRepository {

//  select f.fno, likes(coalesce(r.likes,0)), count(r.reviewsnum)
//  from feeds f left outer join reviews r on f.fno = r.feeds_fno
//  group by f.fno;

  // 영화에 대한 리뷰의 평점과 댓글 갯수를 출력
  @Query("select f, count(r.likes), count(distinct r) " +
      "from Feeds f left outer join Reviews r on r.feeds=f group by f ")
  Page<Object[]> getListPage(Pageable pageable);

  // 아래와 같은 경우 mi를 찾기 위해서 reviews 카운트 만큼 반복횟수도 늘어나는 문제점
  // mi의 pnum이 가장 낮은 이미지 번호가 출력된다.
  // 영화와 영화이미지,리뷰의 평점과 댓글 갯수 출력
  @Query("select f, p, count(r.likes), count(distinct r) from Feeds f " +
      "left outer join Photos p on p.feeds = f " +
      "left outer join Reviews     r  on r.feeds  = f group by f ")
  Page<Object[]> getListPageImg(Pageable pageable);

  // spring 3.x에서는 실행 안됨.
  @Query("select f,max(p),count(r.likes),count(distinct r) from Feeds f " +
      "left outer join Photos p on p.feeds = f " +
      "left outer join Reviews     r  on r.feeds  = f group by f ")
  Page<Object[]> getListPageMaxImg(Pageable pageable);

  // Native Query = SQL
  @Query(value = "select f.fno, p.pnum, p.photos_name, " +
      "count(r.likes), count(r.reviewsnum) " +
      "from db7.photos p left outer join db7.feeds f on f.fno=p.feeds_fno " +
      "left outer join db7.reviews r on f.fno=r.feeds_fno " +
      "where p.pnum = " +
      "(select max(pnum) from db7.photos p2 where p2.feeds_fno=f.fno) " +
      "group by f.fno ", nativeQuery = true)
  Page<Object[]> getListPageImgNative(Pageable pageable);

  // JPQL
  @Query("select f, p, count(r.likes), count(distinct r) from Feeds f " +
      "left outer join Photos p on p.feeds = f " +
      "left outer join Reviews     r  on r.feeds  = f " +
      "where pnum = (select max(p2.pnum) from Photos p2 where p2.feeds=f) " +
      "group by f ")
  Page<Object[]> getListPageImgJPQL(Pageable pageable);

  @Query("select feeds, max(p.pnum) from Photos p group by feeds")
  Page<Object[]> getMaxQuery(Pageable pageable);

  @Query("select f, p, count(r.likes), count(r) " +
      "from Feeds f left outer join Photos p on p.feeds=f " +
      "left outer join Reviews r on r.feeds = f " +
      "where f.fno = :fno group by p ")
  List<Object[]> getFeedsWithAll(Long fno); //특정 영화 조회

}

/*
select p.feeds_fno,pnum from db7.feeds_image p
where p.pnum =
	(select max(mi2.pnum) from db7.feeds_image mi2 where mi2.feeds_fno=p.feeds_fno);
-- 1) 2개의 테이블을 단순 조인, pnum은 먼저 등록된 값으로 나옴
select fno, pnum from db7.feeds_image p, db7.feeds m
where m.fno=p.feeds_fno
group by m.fno order by m.fno desc;

-- 2) pnum은 최근값 출력, img_name은 개별속성이라서 정확히 출력 안됨.
select fno, max(pnum), img_name from db7.feeds_image p, db7.feeds m
where m.fno=p.feeds_fno
group by m.fno order by m.fno desc;

-- 3) 조건절에서 처리하여 pnum, p.img_name들도 불러 올 수 있음
select fno, pnum, p.img_name from db7.feeds_image p, db7.feeds m
where m.fno=p.feeds_fno
and p.pnum = (select max(pnum) from db7.feeds_image mi2 where mi2.feeds_fno=m.fno)
group by m.fno order by m.fno desc;

-- 4) 평점, 댓글 까지 불러옴 하지만, reviews 카운트가 0이면 출력 안됨
select fno, pnum, p.img_name, likes(coalesce(r.likes, 0)), count(coalesce(r.reviewsnum))
from db7.feeds_image p,db7.feeds m,db7.reviews r
where m.fno=p.feeds_fno and m.fno = r.feeds_fno
and p.pnum = (select max(pnum) from db7.feeds_image mi2 where mi2.feeds_fno=m.fno)
group by m.fno order by m.fno desc;

-- 5) 그래서, left outer join을 사용
select m.fno, p.pnum, p.img_name, likes(coalesce(r.likes, 0)), count(r.reviewsnum)
from db7.feeds_image p left outer join db7.feeds m on m.fno=p.feeds_fno
left outer join db7.reviews r on m.fno=r.feeds_fno
where p.pnum = (select max(pnum) from db7.feeds_image mi2 where mi2.feeds_fno=m.fno)
group by m.fno order by m.fno desc;

-- 6) 조건절을 테이블에서도 처리할 수 있음.
select m.fno,m.title, p.pnum, likes(coalesce(r.likes, 0)), count(r.reviewsnum)
from db7.feeds m left outer join
    (select mi2.feeds_fno,mi2.pnum from db7.feeds_image mi2
        where mi2.pnum = (select max(pnum) from db7.feeds_image mi3 where mi2.feeds_fno=mi3.feeds_fno)) as p
on m.fno = p.feeds_fno
left outer join db7.reviews r on r.feeds_fno = m.fno
group by m.fno order by m.fno desc;

*/