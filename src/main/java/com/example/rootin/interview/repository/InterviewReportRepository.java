package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewReport;
import com.example.rootin.interview.domain.InterviewReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewReportRepository extends JpaRepository<InterviewReport, Long> {
    Optional<InterviewReport> findByInterview_Id(Long interviewId);

    @EntityGraph(attributePaths = "interview")
    Page<InterviewReport> findByInterview_Member_IdAndStatus(
            Long memberId,
            InterviewReportStatus status,
            Pageable pageable
    );
}
