package com.example.demo.Controller;

import com.example.demo.DTO.ComentDTO;

import com.example.demo.ServiceComent.ComentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ComentController {

    private final ComentService comentService;

        /**
         * POST /api/coments : 댓글 작성
         * @param comentDTO 댓글 작성 요청 데이터 (boardIdx, userId, ment 포함)
         * @return 작성된 댓글 정보
         */
        @PostMapping("/coments")
        // ⭐️ 반환 타입을 저장된 ComentDTO 객체로 변경하고, 기존 saveComent 메서드를 호출합니다.
        public ResponseEntity<ComentDTO> createComent(@RequestBody ComentDTO comentDTO, Principal principal) {
            if (principal == null || principal.getName() == null) {
                // 로그인 정보가 없는 경우 401 반환
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            try {
                // 1. DTO에 안전한 userId 주입
                String userId = principal.getName();
                comentDTO.setUserId(userId);

                // 2. 서비스 호출 및 저장된 ComentDTO 객체를 반환받음
                ComentDTO savedComent = comentService.saveComent(comentDTO);

                // 3. 댓글 작성 완료 후 201 Created와 함께 객체 반환
                return new ResponseEntity<>(savedComent, HttpStatus.CREATED);

            } catch (IllegalArgumentException e) {
                // 게시글 또는 사용자를 찾을 수 없는 경우
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                // 기타 서버 오류
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        /**
         * GET /api/coments/{idx} : 특정 게시글의 댓글 목록 조회 (AJAX 통신용)
         * ⭐️ 경로를 /api/coments/{idx}로 수정하여 HTML 뷰 로드 경로와 충돌을 피합니다.
         * @param boardIdx 게시글 ID (URL 경로 변수 이름 idx와 매핑)
         * @return 댓글 목록 (JSON)
         */
        @GetMapping("/coments/{idx}")
        public ResponseEntity<List<ComentDTO>> getComents(@PathVariable("idx") Long boardIdx) {
            try {
                List<ComentDTO> coments = comentService.getComentsByBoardId(boardIdx);
                return new ResponseEntity<>(coments, HttpStatus.OK);
            } catch (IllegalArgumentException e) {
                // 게시글을 찾을 수 없을 때 404 Not Found 반환
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        }

        @PutMapping("/coments")
        public ResponseEntity<?> updateComent(@RequestBody ComentDTO comentDTO, Principal principal) {
            if (principal == null || principal.getName() == null) {
                // ⭐️ 로그인 정보가 없는 경우 401 반환 (수정된 부분)
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }

            try {
                // 1. 인증된 사용자의 ID와 DTO의 userId가 일치하는지 확인하는 로직
                String authenticatedUserId = principal.getName();
                if (!authenticatedUserId.equals(comentDTO.getUserId())) {
                    // 권한이 없는 경우 (다른 사용자의 댓글을 수정 시도)
                    return new ResponseEntity<>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN); // 403 Forbidden
                }

                // 2. 서비스 호출하여 댓글 수정
                ComentDTO updatedComent = comentService.updateComent(comentDTO);

                // 3. 성공 응답 반환
                return new ResponseEntity<>(updatedComent, HttpStatus.OK);

            } catch (IllegalArgumentException e) {
                // 댓글을 찾을 수 없거나 (잘못된 idx)
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // 404 Not Found
            } catch (Exception e) {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        /**
         * ⭐️ DELETE /api/coments/{idx} : 댓글 삭제
         * @param comentIdx 삭제할 댓글 ID
         * @param userId 삭제 요청자 ID (쿼리 파라미터로 받음)
         */
        // 실제 운영 환경에서는 @AuthenticationPrincipal 등을 사용해야 하나, 여기서는 예제를 위해 @RequestParam으로 사용자 ID를 받습니다.
        @DeleteMapping("/coments/{comentIdx}")
        public ResponseEntity<?> deleteComent(@PathVariable("comentIdx") Long comentIdx,
                                              @RequestParam("userId") String userId) {
            // DELETE는 Principal을 추가하지 않았으므로, 서비스 로직에서 권한이 없는 경우 던지는 IllegalStateException(403)을 활용합니다.
            try {
                comentService.deleteComent(comentIdx, userId);
                return new ResponseEntity<>("댓글이 성공적으로 삭제되었습니다.", HttpStatus.NO_CONTENT); // 204 No Content
            } catch (IllegalArgumentException e) {
                // 댓글 ID가 유효하지 않거나 찾을 수 없을 때
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // 404 Not Found
            } catch (IllegalStateException e) {
                // 권한이 없을 때 (작성자 ID 불일치)
                return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN); // 403 Forbidden (권한 없음)
            } catch (Exception e) {
                return new ResponseEntity<>("댓글 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

