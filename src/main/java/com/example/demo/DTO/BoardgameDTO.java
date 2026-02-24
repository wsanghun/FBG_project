package com.example.demo.DTO;

import lombok.Data;

@Data
public class BoardgameDTO {
    private String id;
    private int rank;
    private String name;
    private String krName;
    private int year;
    private String description;

    private Object images;
    private String thumbnail;   // 최종 merged 이미지
}