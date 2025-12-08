package com.onlinelearning.service;

import com.onlinelearning.dto.AssignmentDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSubmissionRepository assignmentSubmissionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Assignment createAssignment(AssignmentDTO assignmentDTO) {
        Course course = courseRepository.findById(assignmentDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if current user is the course instructor
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

        // Create notifications for all enrolled students
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

    public List<AssignmentDTO> getAssignmentsByCourse(Long courseId) {
        return assignmentRepository.findByCourseCourseId(courseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Assignment getAssignmentById(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
    }

    @Transactional
    public AssignmentSubmission uploadAssignment(AssignmentSubmission submission) {
        // Verify the student is enrolled in the course
        Assignment assignment = getAssignmentById(submission.getAssignment().getAssignmentId());

        UserAccount currentUser = userService.getCurrentUser();
        if (!(currentUser instanceof Student)) {
            throw new RuntimeException("Only students can submit assignments");
        }

        Student student = (Student) currentUser;

        // Check if student is enrolled in the course
        boolean isEnrolled = assignment.getCourse().getEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getStudent().getUserAccountId().equals(student.getUserAccountId()));

        if (!isEnrolled) {
            throw new RuntimeException("Student is not enrolled in this course");
        }

        // Check if submission already exists
        assignmentSubmissionRepository.findByStudentUserAccountIdAndAssignmentAssignmentId(
                        student.getUserAccountId(), assignment.getAssignmentId())
                .ifPresent(existing -> {
                    throw new RuntimeException("Assignment already submitted");
                });

        submission.setStudent(student);
        submission.setAssignment(assignment);

        return assignmentSubmissionRepository.save(submission);
    }

    @Transactional
    public AssignmentSubmission gradeAssignment(Long studentId, Long assignmentId, Double grade) {
        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByStudentUserAccountIdAndAssignmentAssignmentId(studentId, assignmentId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!submission.getAssignment().getCourse().getInstructor().getUserAccountId()
                .equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can grade assignments");
        }

        submission.setGrade(grade);

        // Create notification for student
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

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!submission.getAssignment().getCourse().getInstructor().getUserAccountId()
                .equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can provide feedback");
        }

        submission.setFeedback(feedback);
        return assignmentSubmissionRepository.save(submission);
    }

    public String getFeedback(Long assignmentId) {
        UserAccount currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Student)) {
            throw new RuntimeException("Only students can view feedback");
        }

        Student student = (Student) currentUser;

        AssignmentSubmission submission = assignmentSubmissionRepository
                .findByStudentUserAccountIdAndAssignmentAssignmentId(
                        student.getUserAccountId(), assignmentId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        return submission.getFeedback();
    }

    public List<AssignmentSubmission> getSubmissions(Long assignmentId) {
        Assignment assignment = getAssignmentById(assignmentId);

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!assignment.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can view submissions");
        }

        return assignmentSubmissionRepository.findByAssignmentAssignmentId(assignmentId);
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