package com.example.rootin.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {

    @NotBlank(message = "refreshToken을 입력해주세요.")
    private String refreshToken;
}
