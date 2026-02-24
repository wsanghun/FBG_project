package com.example.demo.Controller;

import com.example.demo.DTO.BoardgameDTO;
import com.example.demo.ServiceBoardgame.BoardgameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardgameController {

    private final BoardgameService boardgameService;

    // 🔥 전체 보드게임 페이지 (필터 + 리스트 통합)
    @GetMapping("/games")
    public String games(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "rank") String sort,
            @RequestParam(required = false) String keyword,
            Model model
    ) {

        int size = 18; // 페이지당 게임 수

        // 페이징된 리스트 가져오기
        List<BoardgameDTO> games = boardgameService.getPagedGames(page, size, sort, keyword);
        model.addAttribute("games", games);

        model.addAttribute("page", page);
        model.addAttribute("sort", sort);
        model.addAttribute("keyword", keyword);

        int totalCount = boardgameService.getTotalCount(keyword);
        int totalPages = (int) Math.ceil(totalCount / (double) size);
        model.addAttribute("totalPages", totalPages);

        return "boardgames/boardgamelist";
    }

    // 🔥 필터 API (AJAX 요청)
    @GetMapping("/games/filter-api")
    @ResponseBody
    public List<BoardgameDTO> filterGames(
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) List<String> theme,
            @RequestParam(required = false) List<String> language,
            @RequestParam(required = false) List<Integer> minPlayers,
            @RequestParam(required = false) List<Integer> maxTime,
            @RequestParam(required = false) List<Double> maxWeight
    ) {
        return boardgameService.searchGames(
                category, theme, language,
                minPlayers, null, maxTime, maxWeight
        );
    }

    // 🔥 게임 상세 페이지
    @GetMapping("/games/{id}")
    public String gameDetail(@PathVariable String id, Model model) {

        BoardgameDTO game = boardgameService.getAllGames().stream()
                .filter(g -> g.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (game == null) {
            return "redirect:/games";
        }

        model.addAttribute("game", game);
        model.addAttribute("extra", boardgameService.getOrCreateExtra(id));
        model.addAttribute("similarGames", boardgameService.getSimilarGames(id));

        return "boardgames/boardgame-detail";
    }
}
