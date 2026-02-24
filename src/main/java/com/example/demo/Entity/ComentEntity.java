package com.example.demo.Entity;

import com.example.demo.DTO.ComentDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor // JPA에서 필수
@Table(name = "coment")
public class ComentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne
    @JoinColumn(name = "boardidx", referencedColumnName = "idx")
    private BoardEntity board; // 댓글이 속한 게시글

    @ManyToOne
    @JoinColumn(name = "userid", referencedColumnName = "userid") // ⭐️ userId가 아닌 userid로 매핑 가정
    private MemberEntity member; // 댓글 작성자

    @Column(columnDefinition = "TEXT", nullable = false)
    private String ment; // 댓글 내용

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date regdate; // 작성일

    private Long parentidx;
    /**
     * Entity -> DTO 변환 메서드
     */
    public ComentDTO toDTO() {
        String userId = (this.member != null && this.member.getUserid() != null)
                ? this.member.getUserid()
                : "탈퇴 회원";

        // Long 타입 DTO 필드에 맞게 Long으로 반환하도록 수정
        Long boardIdx = (this.board != null && this.board.getIdx() != null)
                ? this.board.getIdx()
                : null;

        return ComentDTO.builder()
                .idx(this.idx)
                .boardIdx(boardIdx) // Long 타입으로 전달
                .userId(userId)
                .ment(this.ment)
                .regdate(this.regdate)
                .parentidx(this.parentidx)// java.util.Date 반환
                .build();
    }

    public void updateMent(String newMent) {
        this.ment = newMent;
    }
}
