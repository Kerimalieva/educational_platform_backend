package com.onlinelearning.repository;

import com.onlinelearning.entity.QuizGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizGradeRepository extends JpaRepository<QuizGrade, Long> {
    List<QuizGrade> findByQuizQuizId(Long quizId);
    Optional<QuizGrade> findByStudentUserAccountIdAndQuizQuizId(Long studentId, Long quizId);
}