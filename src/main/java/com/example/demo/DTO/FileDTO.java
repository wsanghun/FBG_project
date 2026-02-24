package com.example.demo.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data // Getter, Setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함하는 생성자
@Builder
public class FileDTO {
    private Long idx;
    private Long boardIdx;
    private Long comentIdx;
    private String userId;
    private String upfile; // 원본 파일명
    private String newfilename; // 서버 저장 파일명
    private String fileupload; // 파일 저장 경로 (혹은 URL)
    private String type; // 파일 유형 (e.g., "BOARD", "COMENT", "PROFILE")
    private Date regdate;
}
