package com.example.demo.Repository;

import com.example.demo.Entity.MemberEntity;
import com.example.demo.Entity.QMemberEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.demo.Entity.QMemberEntity.memberEntity;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;


    @Override
    public List<MemberEntity> searchMembers(String userid, String name) {


        BooleanExpression sql = null;

        if(userid != null && name != null){
            sql = memberEntity.userid.eq(userid).or(memberEntity.name.eq(name));
        }
        else if (userid != null) {
            sql = memberEntity.userid.eq(userid);
        }
        else if(name != null){
            sql = memberEntity.name.eq(name);
        }
        return jpaQueryFactory.selectFrom(memberEntity).where(sql).fetch();
    }

}
