package com.onlinelearning.repository;

import com.onlinelearning.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentUserAccountId(Long studentId);
    List<Enrollment> findByCourseCourseId(Long courseId);
    Optional<Enrollment> findByStudentUserAccountIdAndCourseCourseId(Long studentId, Long courseId);
    boolean existsByStudentUserAccountIdAndCourseCourseId(Long studentId, Long courseId);
}