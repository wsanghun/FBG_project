package com.example.demo.Interceptor;

import com.example.demo.Entity.MemberEntity;
import com.example.demo.Entity.NotificationEntity;
import com.example.demo.ServiceMember.UserDetailsImpl;
import com.example.demo.ServiceNotification.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationService notificationService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl user) {
            MemberEntity member = user.getMemberEntity();

            // 🔔 안 읽은 알림 수
            int unread = notificationService.getUnreadCount(member);
            request.setAttribute("notificationCount", unread);

            // 🔔 최근 5개 알림 목록
            List<NotificationEntity> list = notificationService.getRecentNotifications(member, 5);
            request.setAttribute("notificationList", list);
        }

        return true;
    }
}
