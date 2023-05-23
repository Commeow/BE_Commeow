package com.example.commeow.global.entity;

import com.example.commeow.entity.Member;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "refreshToken", timeToLive = 3600 * 24 * 14)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    private String refreshToken;

    private String userId;

    public RefreshToken(String refreshToken, Member member) {
        this.refreshToken = refreshToken;
        this.userId = member.getUserId();
    }

    public RefreshToken updateToken(String token) {
        this.refreshToken = token;
        return this;
    }
}
