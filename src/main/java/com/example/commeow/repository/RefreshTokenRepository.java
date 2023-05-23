package com.example.commeow.repository;

import com.example.commeow.global.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(final RefreshToken refreshToken){
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(refreshToken.getRefreshToken(), refreshToken.getUserId());
        redisTemplate.expire(refreshToken.getRefreshToken(), 14L, TimeUnit.DAYS);
    }

    public Optional<RefreshToken> findByUserId(final String refreshToken){
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String userId = (String) valueOperations.get(refreshToken);

        if(userId == null){
            return Optional.empty();
        }
        return Optional.of(new RefreshToken(refreshToken, userId));
    }
}