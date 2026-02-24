package com.example.demo.Controller;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Repository.MemberRepository;
import com.example.demo.ServiceBoard.BoardService;
import com.example.demo.DTO.BoardDTO;
import com.example.demo.ServiceMember.UserDetailsImpl;
import com.example.demo.ServiceSearchLank.SearchLankService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final MemberRepository memberRepository;
    private final SearchLankService searchLankService;


    @GetMapping("/board/write")
    public String write(@AuthenticationPrincipal UserDetailsImpl userDetails,
                        @RequestParam(required = false) String type,
                        Model model,
                        RedirectAttributes rttr) {

        // 로그인 체크
        if (userDetails == null) {
            rttr.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        // 만약 type이 null이면 기본 게시판 free
        if (type == null) type = "free";

        model.addAttribute("type", type);

        return "/board/write";
    }



    @PostMapping("/board_proc")
    public String board_proc(
            @ModelAttribute BoardDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boardService.insert(dto, userDetails);

        return "redirect:/board/boardlist?type=" + dto.getType();
    }

    @GetMapping("/board/allboard")
    public String allBoardPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String word,
            @RequestParam(defaultValue = "latest") String mode,   // ⭐ 추가
            Model model) {

        Page<BoardDTO> allBoards;

        if (mode.equals("popular")) {
            // ⭐ 추천순 인기글 페이징
            allBoards = boardService.getPopularBoardsWithPaging(page, 10, key, word);
        } else {
            // ⭐ 기존 최신순 리스트
            allBoards = boardService.getAllBoardsWithPaging(page, 10, key, word);
        }

        model.addAttribute("allBoards", allBoards);
        model.addAttribute("currentPage", page);
        model.addAttribute("key", key);
        model.addAttribute("word", word);
        model.addAttribute("mode", mode);  // ⭐ 현재 모드 저장

        return "board/allboard";
    }

    @GetMapping(value = "/board/boardlist")
    public String boardlist(Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "5") int size,
                            @RequestParam(value = "key", defaultValue = "") String key,
                            @RequestParam(value = "word", defaultValue = "") String word,
                            @RequestParam(value = "type", defaultValue = "free") String type) {

        Pageable pageable = PageRequest.of(page, 7, Sort.Direction.DESC, "idx");

        // 🔥 타입 기반 목록 조회
        Page<BoardDTO> boardlist = boardService.findAllByType(type, pageable, key, word);

        // 템플릿에 전달
        model.addAttribute("boardlist", boardlist);
        model.addAttribute("key", key);
        model.addAttribute("word", word);
        model.addAttribute("type", type);

        model.addAttribute("popularSearchList", searchLankService.getTopKeywords());

        return "board/boardlist";
    }

    @GetMapping("/board/boardview")
    public String boardView(@RequestParam("idx") Long idx,
                            Model model,
                            Principal principal) {

        boardService.updateViewCount(idx);

        // 🔥 게시글 정보
        BoardDTO boardDTO = boardService.findById(idx);
        model.addAttribute("board", boardDTO);

        // 🔥 로그인 정보
        String loginId = (principal != null) ? principal.getName() : null;
        model.addAttribute("currentUserId", loginId);

        // 🔥 로그인한 사용자의 member_idx 조회 (userId 로 찾기)
        Long memberIdx = null;
        if (loginId != null) {
            memberIdx = memberRepository.findByUserid(loginId)
                    .map(MemberEntity::getIdx)
                    .orElse(null);
        }

        model.addAttribute("currentMemberIdx", memberIdx);

        return "board/boardview";
    }

    @GetMapping("/board/boardmodify")
    public String showModifyForm(@RequestParam("idx") Long idx, Model model, RedirectAttributes redirectAttributes) {

        String currentUserId = boardService.getAuthenticatedUserId();


        try {
            BoardDTO boardDTO = boardService.findById(idx);

            // 1. 권한 검사 (작성자 ID와 현재 사용자 ID 비교)
            if (!boardDTO.getUserid().equals(currentUserId)) {
                redirectAttributes.addFlashAttribute("errorMessage", "수정 권한이 없습니다.");
                return "redirect:/board/view?idx=" + idx;
            }

            model.addAttribute("board", boardDTO);
            model.addAttribute("currentUserId", currentUserId);
            return "board/boardmodify"; // boardmodify.html 템플릿으로 이동

        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/boardlist";
        }
    }

    @PostMapping("/boardmodify_proc")
    public String modifyBoard(@ModelAttribute BoardDTO boardDTO, RedirectAttributes redirectAttributes) {

        String currentUserId = boardService.getAuthenticatedUserId();
        System.out.println(">>> 수정 요청 type = " + boardDTO.getType());

        try {
            // 서비스 계층에서 업데이트 로직 실행 및 권한 재검사
            boardService.updateBoard(boardDTO.getIdx(), boardDTO, currentUserId);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 수정되었습니다.");

            return "redirect:/board/boardview?idx=" + boardDTO.getIdx(); // 수정 후 상세 페이지로 이동

        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "수정할 게시글을 찾을 수 없습니다.");
            return "redirect:/board/boardlist";
        } catch (SecurityException e) {
            // 권한 없음 오류 처리
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/boardview?idx=" + boardDTO.getIdx();
        } catch (Exception e) {
            // 기타 오류 처리
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정 중 오류가 발생했습니다.");
            return "redirect:/board/boardview?idx=" + boardDTO.getIdx();
        }
    }

    @PostMapping("/board/delete")
    public String deletePost(@RequestParam("idx") Long idx,
                             Principal principal,
                             RedirectAttributes rttr) {

        // 1. 로그인 확인
        if (principal == null) {
            rttr.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login"; // 로그인 페이지로 리다이렉트
        }

        String currentUserId = principal.getName();

        try {
            // 2. 서비스 호출 (권한 검사 및 삭제 실행)
            boardService.deletePost(idx, currentUserId);
            rttr.addFlashAttribute("successMessage", idx + "번 게시글이 삭제되었습니다.");
        } catch (SecurityException e) {
            // 3. 권한 부족
            rttr.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/board/boardview?idx=" + idx; // 상세 페이지로 돌아가 오류 메시지 표시
        } catch (EntityNotFoundException e) {
            // 4. 게시글 없음
            rttr.addFlashAttribute("errorMessage", "삭제할 게시글을 찾을 수 없습니다.");
        } catch (Exception e) {
            // 5. 기타 오류
            rttr.addFlashAttribute("errorMessage", "삭제 중 오류가 발생했습니다.");
        }

        // 6. 삭제 성공 또는 실패 시 목록 페이지로 이동
        return "redirect:/board/boardlist";
    }
}
