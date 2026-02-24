package com.example.demo.Controller;

import com.example.demo.Entity.NotificationEntity;
import com.example.demo.Repository.NotificationRepository;
import com.example.demo.ServiceNotification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    @GetMapping("/notifications/read")
    public String readNotification(@RequestParam("id") Long id) {

        // 알림 읽음 처리
        notificationService.markAsRead(id);

        // 알림 내용 조회
        NotificationEntity n = notificationRepository.findById(id).orElse(null);
        if (n == null) return "redirect:/";

        // 댓글 알림 → 댓글로 스크롤 이동
        if (n.getComment() != null) {
            Long boardId = n.getBoard().getIdx();
            Long commentId = n.getComment().getIdx();

            return "redirect:/board/boardview?idx=" + boardId + "#comment-" + commentId;
        }

        // 게시글 알림 → 게시글만 이동
        if (n.getBoard() != null) {
            return "redirect:/board/boardview?idx=" + n.getBoard().getIdx();
        }

        return "redirect:/";
    }
}