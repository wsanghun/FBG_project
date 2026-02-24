package com.example.demo.Controller;

import com.example.demo.DTO.BoardDTO;
import com.example.demo.DTO.BoardgameDTO;
import com.example.demo.DTO.MemberDTO;
import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Repository.BoardRepository;
import com.example.demo.Repository.MemberRepository;
import com.example.demo.ServiceBoard.BoardService;
import com.example.demo.ServiceBoard.MypageService;
import com.example.demo.ServiceBoardgame.BoardgameService;
import com.example.demo.ServiceMember.MemberService; // 인터페이스 주입
import com.example.demo.ServiceSearchLank.SearchLankService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // Pageable import
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault; // PageableDefault import
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Model import
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.demo.ServiceMember.UserDetailsImpl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor // 모든 URL을 "/" 경로를 기준으로 처리
public class MemberController {
    private final MemberService memberService;
    private final BoardgameService boardgameService;
    private final BoardService boardService;
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final MypageService mypageService;
    private final SearchLankService searchLankService;

    @GetMapping("/")
    public String mainPage(Model model) {

        /* 1) 실시간 베스트 게임 */
        List<BoardgameDTO> all = boardgameService.getAllGames();
        List<BoardgameDTO> hot = all.stream()
                .filter(g -> g.getRank() > 0)
                .sorted(Comparator.comparingInt(BoardgameDTO::getRank))
                .limit(20)
                .toList();

        /* 2) 최신글 (DTO 반환) */
        List<BoardDTO> latestList = boardService.getLatestBoards();

        /* 3) 인기글 (자유/후기만, 추천순 + 조회수반영) */
        List<BoardDTO> popularList = boardService.getPopularFreeAndReviewBoards();

        /* 4) 공지사항 */
        List<BoardDTO> noticeList = boardRepository
                .findTop10ByTypeOrderByIdxDesc("notice")
                .stream()
                .map(BoardEntity::toDTO)
                .toList();

        /* model 전달 */
        model.addAttribute("hotGames", hot);
        model.addAttribute("latestList", latestList);
        model.addAttribute("popularList", popularList);
        model.addAttribute("noticeList", noticeList);

        return "main/mainpage";
    }

    @GetMapping("/member/search")
    @ResponseBody
    public List<MemberEntity> search(@RequestParam(value = "userid", defaultValue = "") String userid,
            @RequestParam(value = "name", defaultValue = "") String name) {

        return memberRepository.searchMembers(userid, name);
    }

    @GetMapping("/user/login")
    public String loginForm() {
        return "user/login";
    }

    @GetMapping("/main/mainpage")
    public String mainPage() {

        return "main/mainpage";
    }

    @GetMapping("/mypage/articles")
    public String myArticles(Model model,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(defaultValue = "0") int page) {

        Page<BoardEntity> articles = mypageService.getMyArticles(user.getMemberEntity(),
                PageRequest.of(page, 10) // 10개씩
        );

        model.addAttribute("articles", articles);
        model.addAttribute("popularSearchList", searchLankService.getTopKeywords());

        return "mypage/articles";
    }

    @GetMapping("/user/ssetion")
    @ResponseBody
    public String ssetionCheck() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null)
            return "세션 만료";

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails user = (UserDetails) principal;
            return "로그인 아이디" + user.getUsername() + " " + user.getAuthorities();

        } else {
            return "세션 만료";
        }
    }

    @GetMapping("/member/logout")
    public String logout(HttpSession session, RedirectAttributes rttr) {
        session.invalidate(); // 세션 무효화
        rttr.addFlashAttribute("msg", "로그아웃 되었습니다.");
        return "redirect:/member/login";
    }

    @GetMapping("/user/error_page")
    public String error(Model model, @RequestParam(value = "msg", defaultValue = "error") String msg) {
        System.out.println("/member/error_page");

        System.out.println(">>>> msg: " + msg);
        String url = "/user/login";

        model.addAttribute("msg", msg);
        model.addAttribute("url", url);
        return "user/error_page";
    }

    // @Autowired
    // private MemberService memberService; // DB 처리를 위한 서비스 객체 주입

    // 1. 회원가입 페이지 표시 (GET 요청)
    // URL: /join
    @GetMapping("/user/join")
    public String showJoinForm() {
        // static/User/join.html 파일을 찾아서 사용자에게 반환합니다.
        // Spring Boot는 static 파일의 확장자를 명시해야 합니다.
        return "user/join";
    }

    // 2. 회원가입 폼 데이터 처리 (POST 요청)
    // URL: /joinProcess (join.html 폼의 action 속성과 일치해야 함)
    @PostMapping("/joinProcess")
    public String processJoin(MemberDTO memberDTO) {
        // 폼에서 전송된 데이터는 MemberDTO 객체에 자동으로 바인딩됩니다.

        System.out.println("--- 회원가입 정보 ---");
        System.out.println("아이디: " + memberDTO.getUserid());
        System.out.println("비밀번호: " + memberDTO.getUserpwd());
        System.out.println("이름: " + memberDTO.getName());
        System.out.println("생년월일 (DB 형식): " + memberDTO.getBirth());
        System.out.println("이메일 (DB 형식): " + memberDTO.getEmail());
        System.out.println("기본 권한 (class): " + memberDTO.getlevel());

        try {
            // ⭐ 3. Service 메서드를 호출하여 비밀번호 암호화 및 DB 저장 실행
            memberService.join(memberDTO);

            System.out.println("회원가입 성공!");

        } catch (Exception e) {
            System.out.println("회원가입 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            // DB 저장 실패 시 다시 회원가입 페이지로 돌아가 에러를 표시
            return "redirect:/join?error=true";
        }

        // 회원가입 성공 후 메인 페이지 또는 로그인 페이지로 리다이렉트
        return "redirect:/user/login";
    }

    @GetMapping("/user/memberlist")
    public String getMemberList(
            // PageableDefault를 사용하여 기본 정렬 기준과 페이지 크기 설정
            @PageableDefault(page = 0, size = 10, sort = "idx", direction = Sort.Direction.DESC) Pageable pageable,

            // 검색 키워드 (key: name, id)와 검색어 (word)를 Optional하게 받음
            @RequestParam(required = false, defaultValue = "") String key,
            @RequestParam(required = false, defaultValue = "") String word,
            Model model) {

        // Service를 호출하여 페이지 데이터(검색, 페이징 포함)를 받아옴
        Page<MemberEntity> memberList = memberService.getMemberList(pageable, key, word);

        // 뷰(HTML)에서 사용할 데이터들을 Model에 담기
        model.addAttribute("list", memberList); // 조회된 회원 목록 (Page 객체)
        model.addAttribute("key", key); // 현재 검색 키워드
        model.addAttribute("word", word); // 현재 검색어

        // 검색 필터링된 데이터에 따라 페이지네이션이 작동하도록 HTML에 전달

        // 회원 목록 뷰 파일 경로 반환 (templates/member/list.html이라고 가정)
        return "user/memberlist";
    }

    // ⭐ 4. 회원 상세 조회 기능 (다음 단계에서 사용)
    @GetMapping("/user/view")
    public String viewMember(@RequestParam Long idx, Model model) {
        MemberEntity member = memberService.getMemberByIdx(idx);
        model.addAttribute("member", member);
        return "user/view"; // 상세 페이지 뷰
    }

    @GetMapping("/user/modify")
    public String modifyForm(@RequestParam("idx") Long idx, Model model) {

        // MemberService의 getMemberForModify는 이미 데이터 분리 로직을 처리했다고 가정
        try {
            // 1. 서비스 호출을 한 번만 수행
            MemberDTO memberDTO = memberService.getMemberForModify(idx);

            // 2. DTO를 'dto'라는 이름으로 모델에 담아 폼으로 전달
            model.addAttribute("dto", memberDTO);

            // 3. Thymeleaf 템플릿 반환
            return "user/modify";

        } catch (IllegalArgumentException e) {
            // 4. 회원 정보가 없을 경우 에러 처리
            model.addAttribute("message", e.getMessage());
            // 필요하다면 로그를 남기는 것이 좋습니다.
            return "error/404";
        }
    }

    // 2. 회원 정보 수정 처리 (POST)
    @PostMapping("/user/modifyProcess") // HTML 폼의 action과 일치해야 함
    public String modifyProcess(@ModelAttribute("dto") MemberDTO memberDTO) {

        try {
            memberService.updateMember(memberDTO);

            return "redirect:/user/view?idx=" + memberDTO.getIdx();

        } catch (IllegalArgumentException e) {
            return "redirect:/user/view?idx=" + memberDTO.getIdx();
        }
    }

    @PostMapping("/user/verifyPassword")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, Object> payload) {
        // 1. 요청 본문에서 idx와 userpwd를 추출
        Long idx = Long.valueOf(payload.get("idx").toString());
        String rawPassword = payload.get("userpwd").toString();

        // 2. Service에 검증 위임
        boolean isVerified = memberService.verifyPassword(idx, rawPassword);

        // 3. 결과를 JSON으로 반환
        return ResponseEntity.ok(Map.of("verified", isVerified));
    }

    @GetMapping("/user/delete_proc")
    public String delete(@RequestParam("idx") Long idx) {

        try {
            // 1. 서비스 메서드 호출 (idx를 직접 전달)
            memberService.deleteMember(idx); // ✅ idx를 직접 전달

            // 2. 삭제 성공 후 회원 목록 페이지로 리다이렉트
            // (이전 질문 컨텍스트에 따라 /user/memberlist로 리다이렉트합니다.)
            return "redirect:/user/memberlist"; // ✅ 절대 경로로 수정

        } catch (IllegalArgumentException e) {
            // 3. 회원을 찾지 못한 경우 등의 오류 처리
            System.err.println("삭제 오류: " + e.getMessage());
            // 오류 메시지를 포함하여 목록으로 리다이렉트 (redirect:/user/memberlist?error=notfound)
            return "redirect:/user/memberlist?error=" + e.getMessage();

        } catch (Exception e) {
            // 4. 그 외 서버 내부 오류 처리
            System.err.println("삭제 중 알 수 없는 오류 발생: " + e.getMessage());
            return "redirect:/user/memberlist?error=server";
        }
    }
}
