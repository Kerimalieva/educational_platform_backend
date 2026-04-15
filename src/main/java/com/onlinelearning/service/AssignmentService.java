package com.onlinelearning.service;

import com.onlinelearning.dto.AssignmentDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Assignment createAssignment(AssignmentDTO assignmentDTO) {
        log.info("Creating assignment for courseId: {}", assignmentDTO.getCourseId());
        Course course = courseRepository.findById(assignmentDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can create assignments");
        }

        Assignment assignment = new Assignment();
        assignment.setAssignmentTitle(assignmentDTO.getAssignmentTitle());
        assignment.setAssignmentDescription(assignmentDTO.getAssignmentDescription());
        assignment.setDueDate(assignmentDTO.getDueDate());
        assignment.setCourse(course);

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment saved with id: {}", savedAssignment.getAssignmentId());

        List<Enrollment> enrollments = course.getEnrollments().stream().collect(Collectors.toList());
        for (Enrollment enrollment : enrollments) {
            Notification notification = new Notification(
                    "New assignment posted: " + assignment.getAssignmentTitle(),
                    "assignment",
                    enrollment.getStudent(),
                    savedAssignment.getAssignmentId()
            );
            notificationRepository.save(notification);
        }

        return savedAssignment;
    }

    @Transactional(readOnly = true)
    public List<AssignmentDTO> getAssignmentsByCourse(Long courseId) {
        log.info("=== Fetching assignments for courseId: {} ===", courseId);
        List<Assignment> assignments = assignmentRepository.findByCourseCourseId(courseId);
        log.info("Found {} assignments in DB", assignments.size());
        return assignments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    @Transactional
    public AssignmentSubmission uploadAssignment(AssignmentSubmission submission) {
        Assignment assignment = assignmentRepository.findByIdWithEnrollments(submission.getAssignment().getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        UserAccount currentUser = userService.getCurrentUser();
        if (!(currentUser instanceof Student)) {
            throw new RuntimeException("Only students can submit assignments");
        }
        Student student = (Student) currentUser;

        boolean isEnrolled = assignment.getCourse().getEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getStudent().getUserAccountId().equals(student.getUserAccountId()));
        if (!isEnrolled) {
            throw new RuntimeException("Student is not enrolled in this course");
        }

        if (assignmentSubmissionRepository.findByStudentUserAccountIdAndAssignmentAssignmentId(
                student.getUserAccountId(), assignment.getAssignmentId()).isPresent()) {
            throw new RuntimeException("Assignment already submitted");
        }

        submission.setStudent(student);
        submission.setAssignment(assignment);
        submission.setSubmissionDate(LocalDateTime.now());

        return assignmentSubmissionRepository.save(submission);
    }

    @Transactional
    public AssignmentSubmission gradeAssignment(Long studentId, Long assignmentId, Double grade) {
        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByStudentUserAccountIdAndAssignmentAssignmentId(studentId, assignmentId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        UserAccount currentUser = userService.getCurrentUser();
        if (!submission.getAssignment().getCourse().getInstructor().getUserAccountId()
                .equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can grade assignments");
        }

        submission.setGrade(grade);

        Notification notification = new Notification(
                "Your assignment '" + submission.getAssignment().getAssignmentTitle() +
                        "' has been graded: " + grade,
                "grade",
                submission.getStudent(),
                submission.getSubmissionId()
        );
        notificationRepository.save(notification);

        return assignmentSubmissionRepository.save(submission);
    }

    @Transactional
    public AssignmentSubmission saveAssignmentFeedback(Long studentId, Long assignmentId, String feedback) {
        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByStudentUserAccountIdAndAssignmentAssignmentId(studentId, assignmentId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        UserAccount currentUser = userService.getCurrentUser();
        if (!submission.getAssignment().getCourse().getInstructor().getUserAccountId()
                .equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can provide feedback");
        }

        submission.setFeedback(feedback);
        return assignmentSubmissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public String getFeedback(Long assignmentId) {
        UserAccount currentUser = userService.getCurrentUser();
        if (!(currentUser instanceof Student)) {
            throw new RuntimeException("Only students can view feedback");
        }
        Student student = (Student) currentUser;
        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByStudentUserAccountIdAndAssignmentAssignmentId(student.getUserAccountId(), assignmentId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
        return submission.getFeedback();
    }

    @Transactional(readOnly = true)
    public List<AssignmentSubmission> getSubmissions(Long assignmentId) {
        Assignment assignment = getAssignmentById(assignmentId);
        UserAccount currentUser = userService.getCurrentUser();
        if (!assignment.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can view submissions");
        }
        return assignmentSubmissionRepository.findByAssignmentAssignmentId(assignmentId);
    }

    @Transactional(readOnly = true)
    public AssignmentSubmission findSubmissionByStudentAndAssignment(Long studentId, Long assignmentId) {
        return assignmentSubmissionRepository
                .findByStudentUserAccountIdAndAssignmentAssignmentId(studentId, assignmentId)
                .orElse(null);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId) {
        Assignment assignment = getAssignmentById(assignmentId);
        UserAccount currentUser = userService.getCurrentUser();
        if (!assignment.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can delete assignments");
        }
        assignmentRepository.delete(assignment);
        log.info("Assignment {} deleted", assignmentId);
    }

    private AssignmentDTO convertToDTO(Assignment assignment) {
        AssignmentDTO dto = new AssignmentDTO();
        dto.setAssignmentId(assignment.getAssignmentId());
        dto.setAssignmentTitle(assignment.getAssignmentTitle());
        dto.setAssignmentDescription(assignment.getAssignmentDescription());
        dto.setDueDate(assignment.getDueDate());
        dto.setCourseId(assignment.getCourse().getCourseId());
        dto.setCourseName(assignment.getCourse().getCourseName());
        return dto;
    }
}