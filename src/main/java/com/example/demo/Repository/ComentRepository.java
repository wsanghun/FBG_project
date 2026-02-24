package com.example.demo.Repository;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.ComentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentRepository extends JpaRepository<ComentEntity, Long> {
    List<ComentEntity> findAllByBoard(BoardEntity board);

    List<ComentEntity> findByBoard_IdxOrderByIdxAsc(Long boardidx);

    // ⭐️ [신규] 특정 댓글을 부모로 갖는 자식 댓글이 존재하는지 확인 (삭제 로직을 위해)
    // ComentEntity의 'parentidx' 필드를 기준으로 쿼리합니다.
    boolean existsByParentidx(Long parentidx);
}
