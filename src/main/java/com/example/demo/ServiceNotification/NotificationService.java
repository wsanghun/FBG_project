package com.example.demo.ServiceNotification;

import com.example.demo.Entity.ComentEntity;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Entity.NotificationEntity;

import java.util.List;

public interface NotificationService {
    int getUnreadCount(MemberEntity member);

    List<NotificationEntity> getRecentNotifications(MemberEntity member, int limit);

    void notifyComment(ComentEntity comment);

    // 태그 알림
    void notifyTag(MemberEntity receiver, ComentEntity comment);

    void markAsRead(Long id);
}
