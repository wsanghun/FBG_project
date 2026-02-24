package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받는 사용자
    @ManyToOne
    @JoinColumn(name = "receiver_idx")
    private MemberEntity receiver;

    // 어떤 게시글과 관련?
    @ManyToOne
    @JoinColumn(name = "board_idx")
    private BoardEntity board;

    // 어떤 댓글과 관련?
    @ManyToOne
    @JoinColumn(name = "comment_idx")
    private ComentEntity comment;

    private String message;   // 알림 문구 (예: "홍길동님이 댓글을 남겼습니다.")

    @CreationTimestamp
    private Date createdAt;

    @Column(nullable = false)
    private boolean isRead; // 읽음 여부
}
