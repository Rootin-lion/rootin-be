package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewEvaluationRepository extends JpaRepository<InterviewEvaluation, Long> {
    @Query("""
            SELECT e
            FROM InterviewEvaluation e
            JOIN FETCH e.answerId a
            JOIN FETCH a.questionId q
            JOIN FETCH q.topicId
            WHERE q.interviewId.id = :interviewId
            ORDER BY q.questionOrder
            """)
    List<InterviewEvaluation> findAllByInterviewId(@Param("interviewId") Long interviewId);
}
