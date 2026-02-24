package com.example.demo.Repository;

import com.example.demo.Entity.BoardLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLikeEntity, Long> {


    Optional<BoardLikeEntity> findByBoardIdxAndUserId(Long boardIdx, String userId);

    Long countByBoardIdxAndType(Long boardIdx, BoardLikeEntity.LikeType type);
}
