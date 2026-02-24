package com.example.demo.DTO;

import lombok.Data;
import java.util.List;

@Data
public class BoardgameExtraDTO {

    private double rating;
    private double weight;

    private int minPlayers;
    private int maxPlayers;
    private int playingTime;
    private int minAge;

    private List<String> designers;
    private List<String> publishers;

    private String category;      // 전략, 가족, 파티, 추상, 어린이
    private String theme;         // 동물, 판타지, SF, 경제, 전쟁, 모험
    private String language;
}
