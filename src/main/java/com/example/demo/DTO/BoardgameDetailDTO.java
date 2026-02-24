package com.example.demo.DTO;


import lombok.Data;
import java.util.List;

@Data
public class BoardgameDetailDTO {

    private double rating;       // 평균 평점
    private double weight;       // 난이도
    private int usersRated;      // 평가 인원 수

    private String minPlayers;
    private String maxPlayers;

    private String playingTime;
    private String minAge;

    private List<String> designers;
    private List<String> publishers;
}