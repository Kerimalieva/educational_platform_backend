package com.onlinelearning.repository;

import com.onlinelearning.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourseCourseId(Long courseId);
    List<Quiz> findByCourseCourseIdAndIsActiveTrue(Long courseId);
}