package com.onlinelearning.repository;

import com.onlinelearning.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByInstructorUserAccountId(Long instructorId, Pageable pageable);
}