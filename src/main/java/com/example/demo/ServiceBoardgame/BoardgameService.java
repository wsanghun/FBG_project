package com.example.demo.ServiceBoardgame;


import com.example.demo.DTO.BoardgameDTO;
import com.example.demo.DTO.BoardgameExtraDTO;

import java.util.List;

public interface BoardgameService {
    List<BoardgameDTO> getAllGames();  // 전체 게임 조회

    List<BoardgameDTO> getPagedGames(int page, int size, String sort, String keyword); // 전체 리스트용

    int getTotalCount(String keyword);

    List<BoardgameDTO> getSimilarGames(String id);

    BoardgameExtraDTO getOrCreateExtra(String id);

    List<BoardgameDTO> searchGames(
            List<String> categories,
            List<String> themes,
            List<String> languages,
            List<Integer> minPlayers,
            List<Integer> maxPlayers,
            List<Integer> maxTime,
            List<Double> maxWeight
    );
}