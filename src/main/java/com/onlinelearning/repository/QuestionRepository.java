package com.onlinelearning.repository;

import com.onlinelearning.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizQuizId(Long quizId);
    List<Question> findByCourseCourseId(Long courseId);
}