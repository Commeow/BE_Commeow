package com.example.commeow.service;

import com.example.commeow.dto.SingupRequestDto;
import com.example.commeow.entity.Member;
import com.example.commeow.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Rollback
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원가입 테스트")
    public void testSignup() {
        //given
        SingupRequestDto singupRequestDto = new SingupRequestDto("user111", "User111!!");

        //when
        memberService.signup(singupRequestDto);
        Member member = memberRepository.findByUserId("user111").get();

        //then
        Assertions.assertThat(member.getId()).isEqualTo(1L);
    }
}