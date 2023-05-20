package com.example.commeow.global.jwt;

import com.example.commeow.global.dto.TokenDto;
import com.example.commeow.global.entity.RefreshToken;
import com.example.commeow.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class JwtUtil {

    public static final String ACCESS_TOKEN = "Access_Token";
    public static final String REFRESH_TOKEN = "Refresh_Token";
    private static final Date ACCESS_TIME = (Date) Date.from(Instant.now().plus(1, ChronoUnit.HOURS));
    public static final Date REFRESH_TIME = (Date) Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
    private static final String BEARER_PREFIX = "Bearer ";
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.secret.key}")
    private String SECURITY_KEY;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256; //HS256 암호화 알고리즘 사용

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(SECURITY_KEY);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // 토큰 생성
    public TokenDto createAllToken(String userId) {
        return new TokenDto(createToken(userId, ACCESS_TOKEN),
                createToken(userId, REFRESH_TOKEN));
    }

    public String createToken(String userId, String type) {
        Date date = new Date();
        Date exprTime = type.equals(JwtUtil.ACCESS_TOKEN) ? ACCESS_TIME : REFRESH_TIME;

        return BEARER_PREFIX +
                Jwts.builder()
                        .signWith(key, signatureAlgorithm) //암호화에 사용될 key, 어던 알고리즘으로 key를 암호화할 것인지
                        .setSubject(userId) //subject라는 키에 userId 넣음
                        .setExpiration(exprTime) //토큰 만료시간 설정
                        .setIssuedAt(date) //토큰 생성일
                        .compact();
    }

    // header 토큰 가져오기
    public String resolveToken(HttpServletRequest request, String type) {
        String tokenHeaderName = type.equals(ACCESS_TOKEN) ? ACCESS_TOKEN : REFRESH_TOKEN;

        String bearerToken = request.getHeader(tokenHeaderName);

        //토큰 값이 있는지, 토큰 값이 Bearer 로 시작하는지 판단
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            //Bearer를 자른 값 전달
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // RefreshToken 유효성 검사
    public Boolean refreshTokenValidation(String token) {
        // 토큰 검증
        if (!validateToken(token)) return false;

        // DB에 저장한 refreshToken과 비교
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUserId(getUserInfoFromToken(token));
        return refreshToken.isPresent() && token.equals(refreshToken.get().getRefreshToken().substring(7));
    }

    // 토큰에서 사용자 정보 가져오기
    public String getUserInfoFromToken(String token) {
        // 매개변수로 받은 token을 키를 사용해서 복호화 (디코딩)
        // 복호화된 토큰의 payload에서 subject에 담긴 사용자 정보를 가져옴
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 인증 객체 생성
    public Authentication createAuthentication(String userId) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // AccessToken 헤더 설정
    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader(ACCESS_TOKEN, accessToken);
    }

    // RefreshToken 쿠키 설정
    public void setCookieRefreshToken(HttpServletResponse response, String refreshToken) {
        refreshToken = refreshToken.substring(7);
        Cookie cookie = new Cookie(JwtUtil.REFRESH_TOKEN, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}