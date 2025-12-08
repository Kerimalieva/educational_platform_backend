package com.onlinelearning.repository;

import com.onlinelearning.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByLessonLessonId(Long lessonId);
    Optional<Attendance> findByStudentUserAccountIdAndLessonLessonId(Long studentId, Long lessonId);
}