package com.example.demo.Entity;

import com.example.demo.DTO.BoardDTO;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "board")
public class BoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private String title;

    private String content;

    @ManyToOne
    @JoinColumn(name="userid", referencedColumnName = "userid")
    //@ToString.Exclude
    private MemberEntity member;

    private Long views;

    @Column(nullable = false)
    private String type;

    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date regdate;

    @Column(columnDefinition = "int default 0")
    private int likeCount;

    @Column(columnDefinition = "int default 0")
    private int dislikeCount;

    @OneToMany(mappedBy = "board",           // ComentEntity에서 BoardEntity를 참조하는 필드 이름
            cascade = CascadeType.REMOVE, // ⭐️ 게시글 삭제 시 연결된 댓글도 DB에서 자동 삭제
            orphanRemoval = true)         // 컬렉션에서 제거된 댓글 엔티티도 DB에서 삭제
    @Builder.Default // Lombok @Builder를 사용할 때 리스트 초기화를 위해 필요
    private List<ComentEntity> coments = new ArrayList<>();

    public BoardDTO toDTO(){
        String userid = (this.member != null) ? this.member.getUserid() : "탈퇴 회원" ;

        return BoardDTO.builder()
                .idx(idx)
                .title(title)
                .userid(userid)
                .views(views)
                .content(content)
                .regdate(regdate)
                .type(type)
                .likeCount(likeCount)       // ⭐ 추가
                .dislikeCount(dislikeCount)
                .build();
    }

}
