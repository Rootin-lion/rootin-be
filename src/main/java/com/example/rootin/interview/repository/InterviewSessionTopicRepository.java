package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewSessionTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewSessionTopicRepository extends JpaRepository<InterviewSessionTopic, Long> {
    List<InterviewSessionTopic> findByInterviewIdOrderByTopicOrderAsc(Long interviewId);

    Optional<InterviewSessionTopic> findFirstByInterviewIdAndCompletedFalseOrderByTopicOrderAsc(Long interviewId);

    Optional<InterviewSessionTopic> findByInterviewIdAndTopicOrder(Long interviewId, int topicOrder);
}
