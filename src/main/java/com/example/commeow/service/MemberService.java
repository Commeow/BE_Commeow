package com.example.commeow.service;

import com.example.commeow.dto.LoginRequestDto;
import com.example.commeow.dto.SingupRequestDto;
import com.example.commeow.entity.Member;
import com.example.commeow.global.dto.TokenDto;
import com.example.commeow.global.entity.RefreshToken;
import com.example.commeow.global.jwt.JwtUtil;
import com.example.commeow.repository.MemberRepository;
import com.example.commeow.repository.RefreshTokenRepository;
import com.example.commeow.validator.MemberValidator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberValidator memberValidator;

    /**
     * 회원가입
     */
    public ResponseEntity<String> signup(SingupRequestDto singupRequestDto) {
        memberValidator.validateIsDuplicateMember(singupRequestDto.getUserId());
        // 비밀번호 암호화
        String password = passwordEncoder.encode(singupRequestDto.getPassword());

        memberRepository.save(new Member(singupRequestDto.getUserId(), password));

        return ResponseEntity.ok(null);
    }

    /**
     * 로그인
     */
    public ResponseEntity<String> login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        //유효성 검사
        Member member = memberValidator.validateIsExistMember(loginRequestDto.getUserId());
        memberValidator.validateIsSamePassword(loginRequestDto.getPassword(), member);

        //Token 생성
        TokenDto tokenDto = jwtUtil.createAllToken(member.getUserId());
        setRefreshToken(member, tokenDto);

        setTokenHeader(response, tokenDto);
        return ResponseEntity.ok(null);
    }

    /**
     * 리프레시 토큰 설정
     */
    private void setRefreshToken(Member member, TokenDto tokenDto) {
        //DB에 refreshToken이 있으면 새 토큰으로 업데이트
        //없으면 새로 만들고 DB에 저장
        refreshTokenRepository.findByUserId(member.getUserId()).ifPresentOrElse(
                refreshToken -> {
                    refreshTokenRepository.save(refreshToken.updateToken(tokenDto.getRefreshToken()));
                },
                () -> {
                    String newRefreshToken = tokenDto.getRefreshToken();
                    refreshTokenRepository.save(new RefreshToken(newRefreshToken, member));
                }
        );
    }

    private void setTokenHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader(JwtUtil.ACCESS_TOKEN, tokenDto.getAccessToken());
        response.addHeader(JwtUtil.REFRESH_TOKEN, tokenDto.getRefreshToken());
    }
}
