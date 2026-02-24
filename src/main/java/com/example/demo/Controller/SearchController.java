package com.example.demo.Controller;


import com.example.demo.DTO.BoardgameDTO;
import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.SearchLankEntity;
import com.example.demo.ServiceBoardgame.BoardgameService;
import com.example.demo.ServiceSearch.SearchService;
import com.example.demo.ServiceSearchLank.SearchLankService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService; // 게시판 검색 서비스
    private final BoardgameService boardgameService; // JSON 기반 보드게임 서비스
    private final SearchLankService searchLankService;

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, Model model) {

        searchLankService.saveKeyword(keyword);
        // 게시판 검색
        List<BoardEntity> boardResults = searchService.searchBoards(keyword);

        // 보드게임 검색 (이미 BoardgameService에 포함됨)
        List<BoardgameDTO> gameResults = boardgameService.getPagedGames(1, 9999, "rank", keyword);

        model.addAttribute("keyword", keyword);
        model.addAttribute("boardResults", boardResults);
        model.addAttribute("gameResults", gameResults);

        model.addAttribute("popularSearchList",
                searchLankService.getTopKeywords());


        return "search/searchResult"; // 검색 결과 페이지
    }
}
