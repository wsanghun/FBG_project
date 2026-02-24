package com.example.demo.Repository;

import com.example.demo.Entity.MemberEntity;
import com.example.demo.Entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // 안 읽은 알림 개수
    int countByReceiverAndIsRead(MemberEntity receiver, boolean isRead);

    // 사용자 알림 목록
    List<NotificationEntity> findByReceiverOrderByCreatedAtDesc(MemberEntity receiver);
}
