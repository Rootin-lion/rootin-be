package com.example.rootin.interview.domain;

import com.example.rootin.member.domain.InterestField;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "interview_topic")
public class InterviewTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private InterestField category;

    @Column(name = "topic_name",nullable = false)
    private String topicName;

    @Column(name = "keywords",nullable = false)
    private String keywords;

    @Column(name= "evaluationCriteria", nullable = false)
    private String evaluationCriteria;
}
