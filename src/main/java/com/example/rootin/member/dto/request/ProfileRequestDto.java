package com.example.rootin.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDto {

    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    @NotBlank(message = "연령대를 선택해주세요.")
    @Pattern(regexp = "TEENS|TWENTIES|THIRTIES|FORTIES", message = "올바른 연령대를 선택해주세요.")
    private String ageGroup;

    @NotEmpty(message = "관심분야를 한 개 이상 선택해주세요.")
    @Size(max = 6, message = "관심분야는 최대 6개까지 선택가능합니다.")
    private List<String> interestFields;
}
