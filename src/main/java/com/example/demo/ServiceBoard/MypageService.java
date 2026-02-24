package com.example.demo.ServiceBoard;

import com.example.demo.Entity.BoardEntity;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final BoardRepository boardRepository;


    public Page<BoardEntity> getMyArticles(MemberEntity member, Pageable pageable) {
        return boardRepository.findByMemberOrderByIdxDesc(member, pageable);
    }
}

