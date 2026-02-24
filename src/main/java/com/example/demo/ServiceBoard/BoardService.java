package com.example.demo.ServiceBoard;

import com.example.demo.DTO.BoardDTO;
import com.example.demo.ServiceMember.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoardService {
    BoardDTO insert(BoardDTO dto, UserDetailsImpl userDetails);

    // 삭제! Page<BoardDTO> findAll(Pageable pageable, String key, String word);

    Page<BoardDTO> findAllByType(String type, Pageable pageable, String key, String word);

    BoardDTO findById(Long idx);

    void updateViewCount(Long idx);

    void updateBoard(Long idx, BoardDTO boardDTO, String currentUserId);

    String getAuthenticatedUserId();

    void deletePost(Long idx, String currentUserId);

    List<BoardDTO> getPopularBoards();

    List<BoardDTO> getLatestBoards();

    List<BoardDTO> getPopularFreeAndReviewBoards();

    Page<BoardDTO> getAllBoardsWithPaging(int page, int size, String key, String word);

    Page<BoardDTO> getPopularBoardsWithPaging(int page, int size, String key, String word);
}
