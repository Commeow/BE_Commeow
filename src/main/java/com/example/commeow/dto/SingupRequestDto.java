package com.example.commeow.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SingupRequestDto {

    @Size(min = 4, max = 12)
    private String userId;

    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*?&()_])[A-Za-z\\d$@$!%*?&()_]{8,15}$", message = "{password.pattern}")
    private String password;
}
