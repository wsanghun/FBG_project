package com.example.demo.ServiceNotification;

import com.example.demo.Entity.ComentEntity;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Entity.NotificationEntity;
import com.example.demo.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 💬 댓글 작성 시 알림 생성
    public void notifyComment(ComentEntity comment) {

        MemberEntity receiver = comment.getBoard().getMember(); // 글쓴이
        MemberEntity sender = comment.getMember(); // 댓글 작성자

        // 자기 자신의 글에 댓글을 달면 알림X
        if (receiver.getIdx().equals(sender.getIdx()))
            return;

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .board(comment.getBoard())
                .comment(comment)
                .message(sender.getUserid() + "님이 댓글을 남겼습니다.")
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    // ⭐ 태그 알림 생성
    @Override
    public void notifyTag(MemberEntity receiver, ComentEntity comment) {
        MemberEntity sender = comment.getMember(); // 댓글 작성자

        // 자기 자신을 태그하면 알림 X
        if (receiver.getIdx().equals(sender.getIdx()))
            return;

        NotificationEntity notification = NotificationEntity.builder()
                .receiver(receiver)
                .board(comment.getBoard())
                .comment(comment)
                .message(sender.getUserid() + "님이 댓글에서 회원님을 태그했습니다.")
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }

    // 🔔 안 읽은 알림 개수
    public int getUnreadCount(MemberEntity member) {
        return notificationRepository.countByReceiverAndIsRead(member, false);
    }

    // 🔔 알림 목록
    public List<NotificationEntity> getNotifications(MemberEntity member) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(member);
    }

    // 🔔 읽음 처리
    public void markAsRead(Long id) {
        NotificationEntity n = notificationRepository.findById(id).orElse(null);
        if (n != null) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }

    public List<NotificationEntity> getRecentNotifications(MemberEntity member, int limit) {
        List<NotificationEntity> list = notificationRepository.findByReceiverOrderByCreatedAtDesc(member);
        return list.stream().limit(limit).toList();
    }
}
