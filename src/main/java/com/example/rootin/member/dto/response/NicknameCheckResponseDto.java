package com.example.rootin.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NicknameCheckResponseDto {
    public boolean available;
    public String message;

    public static NicknameCheckResponseDto available() {
        return new NicknameCheckResponseDto(
                true, "사용 가능한 닉네임입니다."
        );
    }

    public static NicknameCheckResponseDto duplicate() {
        return new NicknameCheckResponseDto(
                false, "이미 사용 중인 닉네임입니다."
        );
    }
}
