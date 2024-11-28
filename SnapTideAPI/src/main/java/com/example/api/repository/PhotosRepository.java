package com.example.api.repository;

import com.example.api.entity.Photos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotosRepository extends JpaRepository<Photos, Long> {

  @Modifying
  @Query("delete from Photos p where p.feeds.fno=:fno")
  void deleteByFno(@Param("fno") long fno);

  @Modifying
  @Query("delete from Photos p where p.uuid=:uuid")
  void deleteByUuid(@Param("uuid") String uuid);

  @Query("select p from Photos p where p.feeds.fno=:fno")
  List<Photos> findByMno(@Param("fno") Long fno);

  // 추가: UUID로 Photos를 조회하는 메서드
  @Query("select p from Photos p where p.uuid = :uuid")
  Photos findByUuid(@Param("uuid") String uuid);
}
