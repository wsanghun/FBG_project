package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "board_like")
public class BoardLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private Long boardIdx;

    @Column(name = "userid")
    private String userId;

    @Enumerated(EnumType.STRING)
    private LikeType type; // LIKE / DISLIKE

    @Column(columnDefinition = "datetime")
    private LocalDateTime regdate;

    @PrePersist
    public void onCreate() {
        this.regdate = LocalDateTime.now();
    }

    public enum LikeType {
        like, dislike
    }
}
