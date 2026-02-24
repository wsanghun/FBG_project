package com.example.demo.ServiceMember;

import com.example.demo.Entity.MemberEntity;
import com.example.demo.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("UserDetails oadUserByUsername(String username)");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>" + username);

        Optional<MemberEntity> memberOptional = memberRepository.findByUserid(username);

        // 2. Optional을 처리하여 MemberEntity를 가져옵니다.
        //    만약 사용자가 존재하지 않으면, 표준 예외인 UsernameNotFoundException을 던집니다.
        MemberEntity memberEntity = memberOptional
                .orElseThrow(() -> {
                    // 사용자 정보를 찾을 수 없을 때 발생하는 예외
                    log.error("사용자를 찾을 수 없습니다: {}", username);
                    return new UsernameNotFoundException(username + "을(를) 찾을 수 없습니다.");
                });

        // 3. UserDetailsImpl 객체를 생성하여 반환합니다.
        //    (기존 코드에서 else 구문을 제거하고 깔끔하게 리팩토링)
        return new UserDetailsImpl(memberEntity);
    }


}

