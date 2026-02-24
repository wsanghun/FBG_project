package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_lank")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchLankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    private int count;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
