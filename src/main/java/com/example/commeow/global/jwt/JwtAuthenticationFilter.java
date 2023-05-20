package com.example.commeow.global.jwt;

import com.example.commeow.global.dto.SecurityExceptionDto;
import com.example.commeow.global.entity.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 헤더의 토큰 가져오기
        String accessToken = jwtUtil.resolveToken(request, JwtUtil.ACCESS_TOKEN);
        String refreshToken = jwtUtil.resolveToken(request, JwtUtil.REFRESH_TOKEN);

        // 액세스 토큰 존재 여부 판단
        if (accessToken != null) {
            // 액세스 토큰 유효성 검사
            if (jwtUtil.validateToken(accessToken)) {
                setAuthentication(jwtUtil.getUserInfoFromToken(accessToken));
            }
            // 토큰 만료 && 리프레시 토큰이 존재
            else if (refreshToken != null && jwtUtil.refreshTokenValidation(refreshToken)) {
                String userId = jwtUtil.getUserInfoFromToken(refreshToken);
                // 리프레시 토큰으로 username, Member DB에서 username을 가진 member 가져오기
                String newAccessToken = jwtUtil.createToken(userId, JwtUtil.ACCESS_TOKEN);
                jwtUtil.setHeaderAccessToken(response, newAccessToken);

                setRefreshTokenInCookie(userId, response);
                setAuthentication(userId);
            } else {
                jwtExceptionHandler(response, "토큰이 만료되었습니다.");
                return;
            }
        } else if (refreshToken != null) {
            jwtExceptionHandler(response, "Access 토큰과 Refresh 토큰을 함께 보내주세요.");
            return;
        }
        filterChain.doFilter(request, response);
    }

    // 클라이언트 쿠키 저장소에 refreshToken 저장
    public void setRefreshTokenInCookie(String userId, HttpServletResponse response) {
        String newRefreshToken = jwtUtil.createToken(userId, JwtUtil.REFRESH_TOKEN);
        jwtUtil.setCookieRefreshToken(response, newRefreshToken);
    }

    // SecurityContext 에 Authentication 객체를 저장
    public void setAuthentication(String userId) {
        Authentication authentication = jwtUtil.createAuthentication(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 예외 처리 핸들러
    public void jwtExceptionHandler(HttpServletResponse response, String msg) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        try {
            String json = new ObjectMapper().writeValueAsString(new SecurityExceptionDto(StatusCode.BAD_REQUEST, msg));
            response.getWriter().write(json);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
