package com.example.demo.DTO;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.ComentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComentDTO {
    private Long idx;
    private Long boardIdx;
    private String userId;
    private String ment;
    private Date regdate;
    private Long parentidx;

    /*public ComentEntity toEntity() {

        return ComentEntity.builder()
                .idx(idx)
                .ment(ment)
                .build();
    }*/
}
