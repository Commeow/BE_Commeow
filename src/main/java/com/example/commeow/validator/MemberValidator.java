package com.example.commeow.validator;

import com.example.commeow.entity.Member;
import com.example.commeow.global.exception.ExceptionMessage;
import com.example.commeow.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class MemberValidator {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void validateIsDuplicateMember(String userId) {
        memberRepository.findByUserId(userId).ifPresent(member -> {
            throw new IllegalArgumentException(ExceptionMessage.DUPLICATED_MEMBER.getMessage());
        });
    }

    public Member validateIsExistMember(String userId) {
        return memberRepository.findByUserId(userId).orElseThrow(
                () -> new NoSuchElementException(ExceptionMessage.NO_EXIST_MEMBER.getMessage())
        );
    }

    public void validateIsSamePassword(String password, Member member) {
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException(ExceptionMessage.NO_MATCH_PASSWORD.getMessage());
        }
    }
}
