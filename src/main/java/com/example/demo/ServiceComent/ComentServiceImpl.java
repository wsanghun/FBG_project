package com.example.demo.ServiceComent;

import com.example.demo.DTO.ComentDTO;
import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.ComentEntity;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Repository.BoardRepository;
import com.example.demo.Repository.ComentRepository;
import com.example.demo.Repository.MemberRepository;
import com.example.demo.ServiceNotification.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentServiceImpl implements ComentService {

    private final ComentRepository comentRepository;
    private final BoardRepository boardRepository; // BoardEntity 조회를 위해 필요
    private final MemberRepository memberRepository;
    private final NotificationServiceImpl notificationServiceImpl;// MemberEntity 조회를 위해 필요

    /**
     * 댓글 저장 (작성)
     * 
     * @param comentDTO 댓글 작성 요청 데이터
     * @return 저장된 댓글의 DTO
     */
    @Transactional
    public ComentDTO saveComent(ComentDTO comentDTO) {
        // 1. 참조 엔티티 조회
        BoardEntity board = boardRepository.findById(comentDTO.getBoardIdx())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + comentDTO.getBoardIdx()));

        MemberEntity member = memberRepository.findByUserid(comentDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + comentDTO.getUserId()));

        // 2. DTO -> Entity 변환
        ComentEntity comentEntity = ComentEntity.builder()
                .ment(comentDTO.getMent())
                .board(board)
                .member(member)
                .parentidx(comentDTO.getParentidx())
                .build();

        // 3. 댓글 저장
        ComentEntity savedComent = comentRepository.save(comentEntity);

        // ⭐⭐⭐ 여기! 댓글 저장 후 알림 생성 ⭐⭐⭐
        notificationServiceImpl.notifyComment(savedComent);

        // ⭐⭐⭐ 태그 알림 로직 추가 ⭐⭐⭐
        Pattern pattern = Pattern.compile("@([a-zA-Z0-9가-힣_]+)");
        Matcher matcher = pattern.matcher(comentDTO.getMent());
        Set<String> taggedUserIds = new HashSet<>();

        while (matcher.find()) {
            taggedUserIds.add(matcher.group(1)); // @ 뒤의 아이디 추출
        }

        for (String taggedUserId : taggedUserIds) {
            memberRepository.findByUserid(taggedUserId).ifPresent(taggedMember -> {
                notificationServiceImpl.notifyTag(taggedMember, savedComent);
            });
        }

        // 4. Entity -> DTO 변환
        return savedComent.toDTO();
    }

    /**
     * 특정 게시글의 댓글 목록 조회 (idx 역순 정렬)
     * 
     * @param boardId 게시글 ID
     * @return 댓글 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ComentDTO> getComentsByBoardId(Long boardId) {
        // 1. 게시글 엔티티 조회
        BoardEntity board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + boardId));

        // 2. 해당 게시글의 모든 댓글을 idx 역순(최신 댓글이 위로)으로 조회
        // ⭐️ [수정] findAllByBoardOrderByIdxDesc 메서드를 사용하도록 변경
        List<ComentEntity> comentList = comentRepository.findByBoard_IdxOrderByIdxAsc(boardId);

        // 3. Entity 목록을 DTO 목록으로 변환하여 반환
        return comentList.stream()
                .map(ComentEntity::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 댓글 수정
     * 
     * @param comentDTO 수정할 댓글 DTO (idx, ment, userId 포함)
     * @return 수정된 댓글 DTO
     */
    @Override
    @Transactional
    public ComentDTO updateComent(ComentDTO comentDTO) {
        // 1. 댓글 엔티티 조회
        ComentEntity coment = comentRepository.findById(comentDTO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + comentDTO.getIdx()));

        // 2. 권한 확인: 현재 댓글 작성자와 수정 요청자의 ID가 일치해야 함
        if (!coment.getMember().getUserid().equals(comentDTO.getUserId())) {
            // IllegalStateException은 주로 권한(403)이나 잘못된 상태(409)에 사용될 수 있으므로 기존 로직 유지
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        // 3. 댓글 내용(ment) 수정
        coment.updateMent(comentDTO.getMent());

        // JpaRepository의 save는 인자로 받은 Entity가 ID를 가지고 있으면 UPDATE를 수행합니다.
        // @Transactional이므로 명시적 save 없이 dirty checking으로 업데이트됩니다.
        // ComentEntity updatedComent = comentRepository.save(coment);

        // 4. Entity -> DTO 변환 후 반환
        return coment.toDTO();
    }

    /**
     * 댓글 삭제 (대댓글 존재 시 soft delete)
     * 
     * @param comentIdx 삭제할 댓글 ID
     * @param userId    삭제 요청자 ID
     */
    @Override
    @Transactional
    public void deleteComent(Long comentIdx, String userId) {
        ComentEntity coment = comentRepository.findById(comentIdx)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + comentIdx));

        // 1. 인가 확인: 작성자 ID와 삭제 요청자 ID가 일치하는지 확인
        if (!coment.getMember().getUserid().equals(userId)) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }

        // 2. ⭐️ [신규] 자식 댓글(대댓글) 존재 여부 확인
        if (comentRepository.existsByParentidx(comentIdx)) {
            // 자식 댓글이 있으면: 내용만 "삭제된 댓글입니다."로 수정 (soft delete)
            coment.updateMent("삭제된 댓글입니다.");
            // @Transactional에 의해 자동으로 업데이트됩니다.
        } else {
            // 자식 댓글이 없으면: 실제 DB에서 삭제 (hard delete)
            comentRepository.delete(coment);
        }
    }
}
