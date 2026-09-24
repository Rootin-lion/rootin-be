package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.Interview;
import com.example.rootin.interview.domain.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    // 마이페이지 - 완료한 면접 리스트
    List<Interview> findByMemberIdAndStatusAndCompletedAtIsNotNull(Long memberId, InterviewStatus status);
}
