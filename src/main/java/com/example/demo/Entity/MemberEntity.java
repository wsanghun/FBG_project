package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "MEMBER") // 매핑할 테이블 이름
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, unique = true)
    private  String userid;

    @Column(nullable = false)
    private  String userpwd;

    @Column(nullable = false)
    private  String name;

    private String gender;

    private String birth;

    @Column(nullable = false)
    private String email;

    @CreationTimestamp
    private Date regdate;

    @Column(name = "profile_image")
    private String profileImage;

    private int level;

    public static MemberEntity from(String encodedPassword, String birth, int level) {
        return MemberEntity.builder()
                .userpwd(encodedPassword)
                .birth(birth)
                .level(level)

                .build();
    }
}
