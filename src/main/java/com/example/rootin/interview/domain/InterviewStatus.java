package com.example.rootin.interview.domain;

public enum InterviewStatus {
    CREATED, //방 생성
    DEVICE_VERIFIED, //마이크 및 캠 테스트 완료
    IN_PROGRESS, //면접 진행 중
    PROCESSING, //면접 종료 후 리포트 생성 중
    COMPLETED //리포트 결과 분석 완료
}
