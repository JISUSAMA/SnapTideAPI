package com.example.api.repository;

import com.example.api.entity.Members;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MembersRepository extends JpaRepository<Members, Long> {
  @EntityGraph(attributePaths = {"roleSet"}, type = EntityGraph.EntityGraphType.LOAD)
  @Query("select m from Members m where m.email=:email ")
  Optional<Members> findByEmail(String email);
}
