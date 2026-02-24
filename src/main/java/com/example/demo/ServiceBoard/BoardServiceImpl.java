package com.example.demo.ServiceBoard;

import com.example.demo.DTO.BoardDTO;
import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.ImageEntity;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Repository.BoardRepository;
import com.example.demo.Repository.ImageRepository;
import com.example.demo.Repository.MemberRepository;
import com.example.demo.ServiceImage.ImageServiceImpl;
import com.example.demo.ServiceMember.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService{
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ImageRepository imageRepository;
    private final ImageServiceImpl imageService;


    private List<String> extractImageUrls(String content) {
        List<String> urls = new ArrayList<>();

        if (content == null) {
            return urls;
        }

        Pattern pattern = Pattern.compile("<img[^>]+src=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            urls.add(matcher.group(1));
        }

        return urls;
    }


    public Page<BoardDTO> getPopularBoardsWithPaging(int page, int size, String key, String word) {

        Pageable pageable = PageRequest.of(page - 1, size);

        Page<BoardEntity> result;

        if (word != null && !word.isEmpty()) {
            switch (key) {
                case "title" ->
                        result = boardRepository.searchTitlePopular(word, pageable);
                case "content" ->
                        result = boardRepository.searchContentPopular(word, pageable);
                case "id" ->
                        result = boardRepository.searchWriterPopular(word, pageable);
                default ->
                        result = boardRepository.findPopularExceptNotice(pageable);
            }
        } else {
            result = boardRepository.findPopularExceptNotice(pageable);
        }

        return result.map(BoardEntity::toDTO);
    }

    public Page<BoardDTO> getAllBoardsWithPaging(int page, int size, String key, String word) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "idx"));

        Page<BoardEntity> result;

        if (word != null && !word.isEmpty()) {

            switch (key) {
                case "title" -> result = boardRepository.searchTitle(word, pageable);
                case "content" -> result = boardRepository.searchContent(word, pageable);
                case "id" -> result = boardRepository.searchWriter(word, pageable);
                default -> result = boardRepository.findAllExceptNotice(pageable);
            }

        } else {
            result = boardRepository.findAllExceptNotice(pageable);
        }

        return result.map(BoardEntity::toDTO);
    }

    public List<BoardDTO> getPopularFreeAndReviewBoards() {
        return boardRepository.findPopularFreeAndReviewBoards()
                .stream()
                .map(BoardEntity::toDTO)
                .toList();
    }

    @Override
    public List<BoardDTO> getPopularBoards() {
        return boardRepository.findPopularBoards()
                .stream()
                .map(BoardEntity::toDTO)
                .collect(Collectors.toList());
    }

    public List<BoardDTO> getLatestBoards() {
        return boardRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "idx")))
                .stream()
                .map(BoardEntity::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BoardDTO insert(BoardDTO dto, UserDetailsImpl userDetails) {

        String userId = userDetails.getUsername();

        MemberEntity member = memberRepository.findByUserid(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // ⭐ 0. Base64 이미지 처리 → 파일로 저장, src 교체
        String cleanedContent = imageService.processBase64Images(dto.getContent(), userId);

        // 1. 게시글 저장
        BoardEntity boardEntity = BoardEntity.builder()
                .title(dto.getTitle())
                .content(cleanedContent)
                .member(member)
                .type(dto.getType())
                .views(0L)
                .build();

        BoardEntity savedEntity = boardRepository.save(boardEntity);
        Integer newBoardIdx = savedEntity.getIdx().intValue();

        // 2. 업로드 API(/upload/image)로 저장된 이미지 중 boardIdx == null 인 걸 가져오기
        List<ImageEntity> tempImages =
                imageRepository.findByMember_UseridAndBoardIdxIsNull(userId);

        // 3. 해당 이미지들의 boardIdx 업데이트
        for (ImageEntity img : tempImages) {
            img.setBoardIdx(newBoardIdx);
        }

        // 4. DB 저장
        imageRepository.saveAll(tempImages);

        return savedEntity.toDTO();
    }

    @Override
    public Page<BoardDTO> findAllByType(String type, Pageable pageable, String key, String word) {

        Page<BoardEntity> entityPage;

        // 🔍 검색어가 있을 경우
        if (word != null && !word.trim().isEmpty()) {
            switch (key) {
                case "title":
                    entityPage = boardRepository.findByTitleContainingAndType(word, type, pageable);
                    break;

                case "contents":
                    entityPage = boardRepository.findByContentContainingAndType(word, type, pageable);
                    break;

                case "id":
                    entityPage = boardRepository.findByMemberUseridContainingAndType(word, type, pageable);
                    break;

                default:
                    entityPage = boardRepository.findByType(type, pageable);
                    break;
            }
        }
        // 🔍 검색어가 없으면 전체
        else {
            entityPage = boardRepository.findByType(type, pageable);
        }

        // 🔥 ⭐ BoardDTO + fileCount 세팅해서 반환
        return entityPage.map(entity -> {

            // 1) 기본 DTO로 변환
            BoardDTO dto = entity.toDTO();

            // 2) fileCount 조회 (imageRepository 필요함)
            int cnt = imageRepository.countByBoardIdx(entity.getIdx().intValue());

            // 3) DTO에 설정
            dto.setFileCount(cnt);

            return dto;
        });
    }

    @Override
    public BoardDTO findById(Long idx) {
        // 1. Repository에서 idx로 BoardEntity를 조회합니다.
        // findById는 Optional<T>를 반환합니다.
        Optional<BoardEntity> entityOptional = boardRepository.findById(idx);

        // 2. 엔티티가 존재하면 DTO로 변환하여 반환합니다.
        if (entityOptional.isPresent()) {
            BoardEntity entity = entityOptional.get();
            // toDTO 메서드는 이미 널 체크 로직이 적용되어 있어야 안전합니다.
            return entity.toDTO();
        } else {
            // 엔티티가 존재하지 않으면 예외를 발생시키거나 null을 반환합니다.
            // 여기서는 런타임 예외를 발생시켜 사용자에게 Not Found 페이지를 보여줄 수 있습니다.
            throw new RuntimeException("게시글 번호 " + idx + "를 찾을 수 없습니다.");
            // 또는 return null; (컨트롤러에서 처리)
        }
    }

    @Override
    @Transactional
    public void updateViewCount(Long idx) {
        // Repository를 호출하여 해당 게시글의 view_count 컬럼 값을 1 증가시킵니다.
        boardRepository.updateViewCount(idx);
    }

    @Transactional
    public void updateBoard(Long idx, BoardDTO boardDTO, String currentUserId) {

        // 1. 기존 게시글 Entity를 찾습니다.
        BoardEntity boardEntity = boardRepository.findById(idx)
                .orElseThrow(() -> new NoSuchElementException("수정할 게시글을 찾을 수 없습니다: " + idx));

        // 2. 권한 검사 (현재 로그인된 사용자와 작성자 ID 비교)
        // boardEntity.getMember().getUserid()가 null일 수 있으므로 안전하게 비교
        String writerId = boardEntity.getMember() != null ? boardEntity.getMember().getUserid() : null;

        if (writerId == null || !writerId.equals(currentUserId)) {
            // 작성자 ID가 없거나 현재 사용자와 다르면 예외 발생
            throw new SecurityException("수정 권한이 없습니다.");
        }

        // 3. Entity의 내용을 업데이트합니다.
        // regdate, views 등은 자동으로 유지됩니다.
        boardEntity.setTitle(boardDTO.getTitle());
        boardEntity.setContent(boardDTO.getContent());
        boardEntity.setType(boardDTO.getType());

        // @Transactional 어노테이션 덕분에 save를 명시적으로 호출하지 않아도
        // 엔티티 변경 사항이 자동으로 데이터베이스에 반영됩니다. (Dirty Checking)
    }

    /**
     * 현재 로그인된 사용자 ID를 가져오는 임시 메서드.
     * 실제 환경에서는 Spring Security 등을 사용하여 가져와야 합니다.
     */
    public String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보가 null이 아니고, 익명 사용자(로그인 안 한 사용자)가 아닌 경우에만 ID를 반환합니다.
        if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
            // getName() 메서드는 일반적으로 사용자 ID(Username)를 반환합니다.
            return authentication.getName();
        }
        return null; // 임시로 사용하는 ID
    }

    @Override
    @Transactional
    public void deletePost(Long idx, String currentUserId) {
        // 1. 게시글 존재 여부 확인
        BoardEntity boardEntity = boardRepository.findById(idx)
                .orElseThrow(() -> new EntityNotFoundException("삭제할 게시글을 찾을 수 없습니다. (ID: " + idx + ")"));

        // 2. 권한 검사 (현재 로그인한 사용자 ID와 게시글 작성자 ID 비교)
        String writerId = boardEntity.getMember().getUserid();

        if (currentUserId == null || !currentUserId.equals(writerId)) {
            // 로그인하지 않았거나(currentUserId == null), 작성자가 아닌 경우
            throw new SecurityException("게시글을 삭제할 권한이 없습니다.");
        }

        // 3. 삭제 실행
        boardRepository.deleteById(idx);
    }
}
