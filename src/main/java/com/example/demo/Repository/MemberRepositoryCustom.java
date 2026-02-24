package com.example.demo.Repository;

import com.example.demo.Entity.MemberEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {


    List<MemberEntity> searchMembers(String userid, String name);
}
