package com.example.demo.ServiceMember;

import com.example.demo.DTO.MemberDTO;
import com.example.demo.Entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// ⭐ 인터페이스로 변경
public interface MemberService {

    // 회원가입 메서드 시그니처만 정의 (실제 구현은 Impl에서)
    MemberEntity join(MemberDTO memberDTO);

    Page<MemberEntity> getMemberList(Pageable pageable, String key, String word);

    MemberEntity getMemberByIdx(Long idx);

    // 4. 회원 정보 수정
    void  updateMember(MemberDTO memberDTO);

    boolean verifyPassword(Long idx, String rawPassword);

    MemberDTO getMemberForModify(Long idx);

    // 5. 회원 삭제
    void deleteMember(Long idx);
}
    // List<MemberEntity> getMemberList();
    // MemberEntity updateMember(MemberDTO memberDTO);
    // void deleteMember(Long idx);
