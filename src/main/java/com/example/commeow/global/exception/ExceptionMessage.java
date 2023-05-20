package com.example.commeow.global.exception;

public enum ExceptionMessage {
    DUPLICATED_MEMBER("이미 등록된 회원입니다."),
    DUPLICATED_NICKNAME("중복된 닉네임입니다."),
    NO_EXIST_MEMBER("등록되지 않은 회원입니다"),
    NO_MATCH_PASSWORD("비밀번호가 일치하지 않습니다"),
    HAS_NO_TOKEN("토큰이 존재하지 않습니다."),
    EXPIRED_TOKEN("토큰이 유효하지 않습니다."),
    NOT_LOGIN("로그인한 회원이 아닙니다."),
    NO_AUTHORIZATION("권한이 없습니다.");
    private String message;

    ExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
