package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewQuestion;
import com.example.rootin.interview.domain.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    boolean existsByInterviewId_IdAndTopicId_IdAndQuestionType(Long interviewId, Long topicId, QuestionType questionType);
    long countByInterviewId_Id(Long interviewId); //질문 순서 정할 때 사용
    Optional<InterviewQuestion>
    findFirstByInterviewId_IdAndAnswerIsNullOrderByQuestionOrderDesc(Long interviewId);
}
