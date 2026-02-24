package com.example.demo.Controller;

import com.example.demo.ServiceBoard.BoardLikeService;
import com.example.demo.Entity.BoardEntity;
import com.example.demo.Repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class BoardLikeController {

    private final BoardLikeService likeService;
    private final BoardRepository boardRepository;

    @PostMapping("/{boardIdx}/{userId}/{type}")
    public Map<String, Object> toggleLike(
            @PathVariable Long boardIdx,
            @PathVariable String userId,
            @PathVariable String type) {
        String result = likeService.toggleLike(boardIdx, userId, type);

        BoardEntity board = boardRepository.findById(boardIdx).orElseThrow();

        Map<String, Object> map = new HashMap<>();
        map.put("result", result);
        map.put("likeCount", board.getLikeCount());
        map.put("dislikeCount", board.getDislikeCount());
        return map;
    }
}