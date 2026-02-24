package com.example.demo.ServiceSearch;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.ServiceBoardgame.BoardgameService;
import com.example.demo.DTO.BoardgameDTO;
import com.example.demo.Repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final BoardRepository boardRepository;
    private final BoardgameService boardgameService;

    /**
     * 게시판 검색
     */
    public List<BoardEntity> searchBoards(String keyword) {
        return boardRepository.findByTitleContainingOrContentContaining(keyword, keyword);
    }

    /**
     * 보드게임 검색 (JSON 기반)
     */
    public List<BoardgameDTO> searchGames(String keyword) {
        // BoardgameService의 기존 검색 기능 재사용
        return boardgameService.getPagedGames(1, 9999, "rank", keyword);
    }
}
