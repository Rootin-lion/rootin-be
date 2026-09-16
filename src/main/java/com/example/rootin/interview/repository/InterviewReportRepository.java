package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {
    Optional<InterviewReport> findByInterviewId_Id(Long interviewId);

    boolean existsByInterviewId_Id(Long interviewId);
}
