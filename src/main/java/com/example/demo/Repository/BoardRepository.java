package com.example.demo.Repository;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
    /*List<BoardEntity> findByNameContaining(String word);*/

    Page<BoardEntity> findByType(String type, Pageable pageable);

    // 검색 + type 필터링 (제목)
    Page<BoardEntity> findByTitleContainingAndType(String title, String type, Pageable pageable);

    // 내용 검색 + type 필터링
    Page<BoardEntity> findByContentContainingAndType(String content, String type, Pageable pageable);

    // 작성자 검색 + type 필터링
    Page<BoardEntity> findByMemberUseridContainingAndType(String userid, String type, Pageable pageable);

    List<BoardEntity> findByTitleContainingOrContentContaining(String title, String content);

    List<BoardEntity> findTop10ByOrderByViewsDesc();

    List<BoardEntity> findTop10ByTypeOrderByIdxDesc(String type);

    Page<BoardEntity> findByMemberOrderByIdxDesc(MemberEntity member, Pageable pageable);

    @Modifying // 데이터를 변경하는 쿼리임을 명시
    @Query("update BoardEntity b set b.views = b.views + 1 where b.idx = :idx")
    void updateViewCount(@Param("idx") Long idx);

    @Transactional
    @Modifying
    @Query("UPDATE BoardEntity b SET b.likeCount = :likeCount, b.dislikeCount = :dislikeCount WHERE b.idx = :boardIdx")
    void updateLikeDislikeCount(@Param("boardIdx") Long boardIdx,
                                @Param("likeCount") Long likeCount,
                                @Param("dislikeCount") Long dislikeCount);

    @Query("SELECT b FROM BoardEntity b " +
            "ORDER BY (b.likeCount - b.dislikeCount) DESC, b.views DESC")
    List<BoardEntity> findPopularBoards();

    @Query("SELECT b FROM BoardEntity b " +
            "WHERE b.type = 'free' OR b.type = 'review' " +
            "ORDER BY (b.likeCount - b.dislikeCount) DESC, b.views DESC")
    List<BoardEntity> findPopularFreeAndReviewBoards();

    @Query("SELECT b FROM BoardEntity b WHERE b.type <> 'notice'")
    Page<BoardEntity> findAllExceptNotice(Pageable pageable);

    @Query("SELECT b FROM BoardEntity b WHERE b.type <> 'notice' AND b.title LIKE %:word%")
    Page<BoardEntity> searchTitle(@Param("word") String word, Pageable pageable);

    @Query("SELECT b FROM BoardEntity b WHERE b.type <> 'notice' AND b.content LIKE %:word%")
    Page<BoardEntity> searchContent(@Param("word") String word, Pageable pageable);

    @Query("SELECT b FROM BoardEntity b WHERE b.type <> 'notice' AND b.member.userid LIKE %:word%")
    Page<BoardEntity> searchWriter(@Param("word") String word, Pageable pageable);

    @Query("SELECT b FROM BoardEntity b WHERE b.type <> 'notice' ORDER BY b.likeCount DESC")
    Page<BoardEntity> findPopularExceptNotice(Pageable pageable);

    @Query("SELECT b FROM BoardEntity b WHERE b.type <> 'notice' AND b.title LIKE %:word% ORDER BY b.likeCount DESC")
    Page<BoardEntity> searchTitlePopular(@Param("word") String word, Pageable pageable);

    @Query("SELECT b FROM BoardEntity b WHERE b.type <> 'notice' AND b.content LIKE %:word% ORDER BY b.likeCount DESC")
    Page<BoardEntity> searchContentPopular(@Param("word") String word, Pageable pageable);

    @Query("SELECT b FROM BoardEntity b WHERE b.type <> 'notice' AND b.member.userid LIKE %:word% ORDER BY b.likeCount DESC")
    Page<BoardEntity> searchWriterPopular(@Param("word") String word, Pageable pageable);

}
