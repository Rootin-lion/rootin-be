package com.example.rootin.interview.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class InterviewAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    public static InterviewAnswer create(InterviewQuestion question, String answer) {
        InterviewAnswer interviewAnswer = new InterviewAnswer();

        interviewAnswer.question = question;
        interviewAnswer.answer = answer;
        interviewAnswer.answeredAt = LocalDateTime.now();

        return interviewAnswer;
    }
}
