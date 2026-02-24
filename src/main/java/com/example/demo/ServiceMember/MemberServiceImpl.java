package com.example.demo.ServiceMember;

import com.example.demo.DTO.MemberDTO;
import com.example.demo.Entity.MemberEntity;
import com.example.demo.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
// ⭐ MemberService 인터페이스를 구현합니다.
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 기능 구현
     */
    @Override // ⭐ 인터페이스 메서드를 구현함을 명시
    public MemberEntity join(MemberDTO memberDTO) {

        String encodedPwd = passwordEncoder.encode(memberDTO.getUserpwd());

        MemberEntity memberEntity = MemberEntity.builder()
                .userid(memberDTO.getUserid())
                .userpwd(encodedPwd)
                .name(memberDTO.getName())
                .gender(memberDTO.getGender())
                .birth(memberDTO.getBirth())
                .email(memberDTO.getEmail())
                .level(memberDTO.getlevel())
                .build();

        return memberRepository.save(memberEntity);
    }

    @Override
    public Page<MemberEntity> getMemberList(Pageable pageable, String key, String word) {

        // 검색어가 비어 있거나 key가 설정되지 않은 경우 (전체 목록 조회)
        if (word == null || word.trim().isEmpty() || key == null || key.trim().isEmpty()) {
            // JpaRepository의 findAll(Pageable)을 호출
            return memberRepository.findAll(pageable);
        }

        // 검색 조건이 있는 경우
        String searchWord = word.trim();

        switch (key) {
            case "id":
                // 아이디(userid) 검색
                return memberRepository.findByUseridContaining(searchWord, pageable);
            case "name":
                // 이름(name) 검색
                return memberRepository.findByNameContaining(searchWord, pageable);
            // 다른 검색 조건이 있다면 여기에 추가합니다.
            default:
                // 유효하지 않은 key인 경우 전체 목록 반환
                return memberRepository.findAll(pageable);
        }
    }

    // --- 3. 회원 상세 조회 기능 구현 ---
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
    public MemberEntity getMemberByIdx(Long idx) {
        // idx를 이용하여 회원을 조회합니다. 없으면 예외 발생
        return memberRepository.findById(idx)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. idx: " + idx));
    }

    // --- 4. 회원 정보 수정 기능 (추후 구현) ---
    @Override
    @Transactional(readOnly = true)
    public MemberDTO getMemberForModify(Long idx) {
        MemberEntity entity = memberRepository.findById(idx)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. idx: " + idx));

        // 1. DTO 객체 생성 (Builder나 생성자를 사용해도 무방)
        MemberDTO dto = new MemberDTO();

        // DTO에 기본 정보 채우기
        dto.setIdx(entity.getIdx());
        dto.setUserid(entity.getUserid());
        dto.setName(entity.getName());
        dto.setGender(entity.getGender());
        dto.setClassValue(entity.getLevel());

        // 2. ⭐ 생년월일 분리 후 DTO의 전용 필드(birthYear, birthMonth, birthDay)에 설정 ⭐
        String birth = entity.getBirth();
        if (birth != null && birth.contains("-")) {
            String[] parts = birth.split("-");
            if (parts.length == 3) {
                dto.setYy(parts[0]);
                dto.setMm(parts[1]);
                dto.setDd(parts[2]);
            }
        }

        // 3. ⭐ 이메일 분리 후 DTO의 전용 필드(emailPart1, emailPart2)에 설정 ⭐
        String fullEmail = entity.getEmail();
        if (fullEmail != null && fullEmail.contains("@")) {
            String[] parts = fullEmail.split("@", 2);
            dto.setEmailprefix(parts[0]);
            if (parts.length > 1) {
                dto.setEmaildomain(parts[1]);
            }
        }

        // ⭐ 중요: DTO에 setBirth(String)과 setEmail(String) 필드가 있다면 주석 처리하거나 제거
        // dto.setBirth(entity.getBirth());
        // dto.setEmail(entity.getEmail());

        return dto;
    }

    @Override
    public void updateMember(MemberDTO memberDTO) { // ⭐ 반환 타입을 void로, 메서드 이름을 modifyMember로 변경
        MemberEntity entity = memberRepository.findById(memberDTO.getIdx())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. idx: " + memberDTO.getIdx()));

        // 2. 비밀번호 변경 확인
        String newPassword = memberDTO.getUserpwd();
        if (newPassword != null && !newPassword.isEmpty()) {
            // 새 비밀번호가 있다면 인코딩하여 설정
            String encodedPassword = passwordEncoder.encode(newPassword);
            entity.setUserpwd(encodedPassword);
        }
        // *참고: 비밀번호가 없으면 기존 비밀번호가 유지됨.

        // 3. DTO로부터 업데이트된 데이터 설정 (아이디는 수정 불가, 이름부터)
        entity.setName(memberDTO.getName());
        entity.setGender(memberDTO.getGender());
        entity.setLevel(memberDTO.getlevel()); // level이 수정 대상이라면

        // 이메일 합치기 (DTO의 getEmail() 사용)
        entity.setEmail(memberDTO.getEmail());

        // 생년월일 합치기 (DTO의 getBirth() 사용)
        entity.setBirth(memberDTO.getBirth());

        // save() 호출 없이 @Transactional에 의해 자동 업데이트(Dirty Checking)됨
        // 반환할 값이 없으므로 return 문은 생략합니다.
    }

    @Override
    public boolean verifyPassword(Long idx, String rawPassword) {
        // 1. DB에서 해당 회원의 정보(암호화된 비밀번호)를 가져옵니다.
        MemberEntity entity = memberRepository.findById(idx)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다."));

        String encodedPassword = entity.getUserpwd(); // DB에 저장된 암호화된 비밀번호

        // 2. 사용자가 입력한 평문 비밀번호와 DB의 암호화된 비밀번호를 비교합니다.
        // ⭐⭐ 이 부분이 암호화된 비밀번호 비교의 핵심입니다. ⭐⭐
        // `passwordEncoder.matches(평문 비밀번호, 암호화된 비밀번호)`
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // --- 5. 회원 삭제 기능 ---
    @Override
    public void deleteMember(Long idx) {
        // Spring Data JPA의 findById는 Optional<T>를 반환합니다.
        Optional<MemberEntity> optionalEntity = memberRepository.findById(idx);

        // 2. 엔티티 존재 여부 확인 및 예외 처리
        if (optionalEntity.isEmpty()) {
            // 해당 ID의 회원이 존재하지 않을 경우 예외를 발생시킵니다.
            throw new IllegalArgumentException("해당하는 회원 정보(ID: " + idx + ")를 찾을 수 없습니다."); // ✅ idx 사용
        }

        // 3. 엔티티 삭제
        MemberEntity entity = optionalEntity.get();
        memberRepository.delete(entity);

        }
}

