package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {
    boolean existsByQuestion_Id(Long questionId);
}
