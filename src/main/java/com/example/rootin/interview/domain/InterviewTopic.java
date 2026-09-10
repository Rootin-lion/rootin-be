package com.example.rootin.interview.domain;

import com.example.rootin.member.domain.InterestField;
import com.example.rootin.interview.domain.converter.StringListToJsonConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name = "interview_topic")
@NoArgsConstructor
public class InterviewTopic { //질문 생성 용 topic 데이터베이스
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private InterestField category;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @Convert(converter = StringListToJsonConverter.class)
    @Column(name = "keywords", columnDefinition = "TEXT")
    private List<String> keywords;

}
