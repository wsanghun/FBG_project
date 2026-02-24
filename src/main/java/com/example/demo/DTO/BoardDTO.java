package com.example.demo.DTO;

import com.example.demo.Entity.BoardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDTO {
    private Long idx;
    private String title;
    private String content;
    private String userid;
    private Date regdate;
    private Long views;
    private String key;
    private String word;
    private String type;

    private int likeCount;     // ⭐ 추가
    private int dislikeCount;

    // ⭐ 추가
    private int fileCount;

}
