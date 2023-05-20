package com.example.commeow.global.security;

import com.example.commeow.entity.Member;
import com.example.commeow.global.exception.ExceptionMessage;
import com.example.commeow.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionMessage.NO_EXIST_MEMBER.getMessage()));

        return new UserDetailsImpl(member, member.getUserId());
    }
}
