package com.onlinelearning.repository;

import com.onlinelearning.entity.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignmentAssignmentId(Long assignmentId);
    Optional<AssignmentSubmission> findByStudentUserAccountIdAndAssignmentAssignmentId(Long studentId, Long assignmentId);
}