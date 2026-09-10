package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findByInterviewIdOrderByQuestionOrderAsc(Long interviewId);

}
