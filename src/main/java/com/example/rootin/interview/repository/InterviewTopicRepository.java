package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewTopic;
import com.example.rootin.member.domain.InterestField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface InterviewTopicRepository extends JpaRepository<InterviewTopic, Long> {
    Optional<InterviewTopic> findFirstByCategoryOrderByIdAsc(InterestField category);
    List<InterviewTopic> findAllByCategoryOrderByIdAsc(InterestField category);
    Page<InterviewTopic> findByCategoryOrderByIdAsc(InterestField category, Pageable pageable);
}
