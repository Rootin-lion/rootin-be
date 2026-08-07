package com.example.rootin.interview.domain;

import com.example.rootin.member.domain.InterestField;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@Table(name = "interview_topic")
@NoArgsConstructor
public class InterviewTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private InterestField category;

    @Column(name = "topic_name", nullable = false)
    private String topicName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "interview_topic_keywords", joinColumns = @JoinColumn(name = "interview_topic_id"))
    @Column(name = "keyword", nullable = false)
    private List<String> keywords;

}
