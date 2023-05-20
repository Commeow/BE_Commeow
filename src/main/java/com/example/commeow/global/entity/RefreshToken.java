package com.example.commeow.global.entity;

import com.example.commeow.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
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
