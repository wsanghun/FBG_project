package com.example.demo.ServiceMember;

import com.example.demo.Entity.MemberEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private MemberEntity memberEntity;

    public UserDetailsImpl(MemberEntity memberEntity){

        this.memberEntity = memberEntity;

    }

    public String getProfileImage() {
        return memberEntity.getProfileImage();
    }

    public MemberEntity getMemberEntity() {
        return this.memberEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if(memberEntity.getLevel() > 9)
        {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
        }
        else
        {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return authorities;
    }

    @Override
    public String getPassword(){
        return memberEntity.getUserpwd();
    }

    @Override
    public String getUsername(){
        return memberEntity.getUserid();
    }

    public String getName(){
        return memberEntity.getName();
    }


}