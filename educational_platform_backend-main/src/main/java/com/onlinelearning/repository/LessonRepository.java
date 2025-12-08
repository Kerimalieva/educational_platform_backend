package com.onlinelearning.repository;

import com.onlinelearning.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByCourseCourseIdOrderByLessonOrderAsc(Long courseId);
}