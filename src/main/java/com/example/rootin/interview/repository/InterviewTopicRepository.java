package com.example.rootin.interview.repository;

import com.example.rootin.interview.domain.InterviewTopic;
import com.example.rootin.member.domain.InterestField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewTopicRepository extends JpaRepository<InterviewTopic, Long> {
    @Query(
            value = """
                    SELECT *
                    FROM interview_topic
                    WHERE category = :category
                    ORDER BY RAND()
                    LIMIT :questionCount
                    """,
            nativeQuery = true
    )
    List<InterviewTopic> findRandomTopicsByField(@Param("category")String category, @Param("questionCount") int questionCount);
}
