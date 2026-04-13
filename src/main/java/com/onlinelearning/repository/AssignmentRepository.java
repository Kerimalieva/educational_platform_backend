package com.onlinelearning.repository;

import com.onlinelearning.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseCourseId(Long courseId);

    @Query("SELECT a FROM Assignment a JOIN FETCH a.course c JOIN FETCH c.enrollments WHERE a.assignmentId = :id")
    Optional<Assignment> findByIdWithEnrollments(@Param("id") Long id);
}