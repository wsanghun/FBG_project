package com.example.demo.Repository;

import com.example.demo.Entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long>, MemberRepositoryCustom {

    Optional<MemberEntity> findByUserid(String userid);

    /*List<MemberEntity> findByUseridContaining(String userid, String name);*/

    Page<MemberEntity> findByUseridContaining(String userid, Pageable pageable);

    // ⭐ 2. 이름(name) 검색을 위한 쿼리 메서드 정의 추가
    Page<MemberEntity> findByNameContaining(String name, Pageable pageable);
    // MemberEntity findById(String id);
}
