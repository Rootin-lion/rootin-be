package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    Optional<InterviewAnswer> findByInterviewQuestion_Id(Long questionId);

    @Query("""
            select answer
            from InterviewAnswer answer
            where answer.interviewQuestion.interview.id = :interviewId
            order by answer.interviewQuestion.questionOrder asc
            """)
    List<InterviewAnswer> findAllByInterviewIdOrderByQuestionOrder(@Param("interviewId") Long interviewId);
}
