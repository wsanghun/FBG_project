package com.example.demo.Repository;

import com.example.demo.Entity.SearchLankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SearchLankRepository extends JpaRepository<SearchLankEntity, Long> {

    Optional<SearchLankEntity> findByKeyword(String keyword);

    @Query("SELECT s FROM SearchLankEntity s ORDER BY s.count DESC, s.updatedAt DESC")
    List<SearchLankEntity> findTop5ByOrderByCountDesc();
}
