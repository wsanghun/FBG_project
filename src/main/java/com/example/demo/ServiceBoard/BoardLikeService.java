package com.example.demo.ServiceBoard;

import com.example.demo.Entity.BoardLikeEntity;
import com.example.demo.Repository.BoardLikeRepository;
import com.example.demo.Repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardLikeService {

    private final BoardLikeRepository likeRepository;
    private final BoardRepository boardRepository;

    public String toggleLike(Long boardIdx, String userId, String type) {

        BoardLikeEntity.LikeType likeType =
                type.equals("like") ? BoardLikeEntity.LikeType.like : BoardLikeEntity.LikeType.dislike;

        Optional<BoardLikeEntity> existing =
                likeRepository.findByBoardIdxAndUserId(boardIdx, userId);

        if (existing.isPresent()) {
            BoardLikeEntity like = existing.get();

            // 같은 버튼을 다시 누른 경우 → 추천 취소
            if (like.getType() == likeType) {
                likeRepository.delete(like);
                updateBoardCounts(boardIdx);
                return "removed";
            }

            // 다른 타입으로 변경
            like.setType(likeType);
            likeRepository.save(like);
            updateBoardCounts(boardIdx);
            return "updated";
        }

        // 처음 누르는 경우
        BoardLikeEntity newLike = new BoardLikeEntity();
        newLike.setBoardIdx(boardIdx);
        newLike.setUserId(userId);
        newLike.setType(likeType);
        likeRepository.save(newLike);

        updateBoardCounts(boardIdx);
        return "added";
    }

    // 좋아요/싫어요 카운트 업데이트
    public void updateBoardCounts(Long boardIdx) {

        Long likeCount = likeRepository.countByBoardIdxAndType(
                boardIdx, BoardLikeEntity.LikeType.like
        );
        Long dislikeCount = likeRepository.countByBoardIdxAndType(
                boardIdx, BoardLikeEntity.LikeType.dislike
        );

        boardRepository.updateLikeDislikeCount(boardIdx, likeCount, dislikeCount);
    }
}

