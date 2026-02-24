package com.example.demo.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx")
    private Integer idx;

    // 게시글 번호 (외래키로 사용할 수 있음)
    @Column(name = "boardidx")
    private Integer boardIdx;

    @ManyToOne
    @JoinColumn(name = "userid", referencedColumnName = "userid") // ⭐️ userId가 아닌 userid로 매핑 가정
    private MemberEntity member;

    @Column(name = "filename", length = 200)
    private String fileName;

    @Column(name = "originalname", length = 200)
    private String originalName;

    @Column(name = "fileurl", length = 200)
    private String fileUrl;

    @Column(name = "filesize")
    private Integer fileSize;

    @Column(name = "regdate")
    private LocalDateTime regDate;

    @Column(name = "type", length = 20)
    private String type;

    @PrePersist
    public void onCreate() {
        this.regDate = LocalDateTime.now();
    }
}