package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    boolean existsByInterview_IdAndTopic_IdAndQuestionType(Long interviewId, Long topicId, QuestionType questionType);
    long countByInterview_Id(Long interviewId); //질문 순서 정할 때 사용
    Optional<InterviewQuestion>
    findFirstByInterview_IdAndAnswerIsNullOrderByQuestionOrderDesc(Long interviewId);

    @EntityGraph(attributePaths = "topic")
    @Query("""
            SELECT question
            FROM InterviewQuestion question
            WHERE question.interview.id IN :interviewIds
              AND question.questionType = com.example.rootin.interview.domain.QuestionType.BASIC
            ORDER BY question.questionOrder ASC
            """)
    List<InterviewQuestion> findBasicQuestionsByInterviewIds(
            @Param("interviewIds") List<Long> interviewIds
    );
}
