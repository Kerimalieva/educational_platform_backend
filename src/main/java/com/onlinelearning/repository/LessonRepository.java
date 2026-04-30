package com.onlinelearning.repository;

import com.onlinelearning.entity.Lesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Page<Lesson> findByCourseCourseIdOrderByLessonOrderAsc(Long courseId, Pageable pageable);

    List<Lesson> findByCourseCourseIdOrderByLessonOrderAsc(Long courseId);
}