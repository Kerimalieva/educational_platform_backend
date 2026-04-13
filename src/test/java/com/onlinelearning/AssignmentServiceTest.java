package com.onlinelearning;

import com.onlinelearning.dto.AssignmentDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import com.onlinelearning.service.AssignmentService;
import com.onlinelearning.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private AssignmentSubmissionRepository submissionRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserService userService;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Course course;
    private Instructor instructor;
    private Student student;
    private Assignment assignment;
    private AssignmentDTO assignmentDTO;

    @BeforeEach
    void setUp() {
        instructor = new Instructor();
        instructor.setUserAccountId(100L);
        instructor.setFirstName("John");
        instructor.setLastName("Doe");

        course = new Course();
        course.setCourseId(1L);
        course.setCourseName("Spring Boot");
        course.setInstructor(instructor);
        course.setEnrollments(Set.of());

        student = new Student();
        student.setUserAccountId(200L);
        student.setFirstName("Alice");
        student.setLastName("Smith");

        assignment = new Assignment();
        assignment.setAssignmentId(10L);
        assignment.setAssignmentTitle("Test Assignment");
        assignment.setCourse(course);

        assignmentDTO = new AssignmentDTO();
        assignmentDTO.setAssignmentTitle("New Assignment");
        assignmentDTO.setAssignmentDescription("Desc");
        assignmentDTO.setDueDate(LocalDateTime.now().plusDays(7));
        assignmentDTO.setCourseId(1L);
    }

    @Test
    void createAssignment_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(assignmentRepository.save(any(Assignment.class))).thenReturn(assignment);

        Assignment result = assignmentService.createAssignment(assignmentDTO);

        assertNotNull(result);
        assertEquals("Test Assignment", result.getAssignmentTitle());
        verify(assignmentRepository).save(any(Assignment.class));
        verify(notificationRepository, never()).save(any()); // no enrollments
    }

    @Test
    void createAssignment_UserNotInstructor_ThrowsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student); // student instead of instructor

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> assignmentService.createAssignment(assignmentDTO));
        assertEquals("Only course instructor can create assignments", ex.getMessage());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_CourseNotFound_ThrowsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> assignmentService.createAssignment(assignmentDTO));
        assertEquals("Course not found", ex.getMessage());
    }

    @Test
    void uploadAssignment_Success() {
        // Arrange
        Enrollment enrollment = new Enrollment(student, course);
        course.setEnrollments(Set.of(enrollment));

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setSubmittedContent("content");

        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(userService.getCurrentUser()).thenReturn(student);
        when(submissionRepository.findByStudentUserAccountIdAndAssignmentAssignmentId(200L, 10L))
                .thenReturn(Optional.empty());
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenReturn(submission);

        AssignmentSubmission result = assignmentService.uploadAssignment(submission);

        assertNotNull(result);
        verify(submissionRepository).save(any(AssignmentSubmission.class));
    }

    @Test
    void uploadAssignment_StudentNotEnrolled_ThrowsException() {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);

        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(userService.getCurrentUser()).thenReturn(student);
        // course.getEnrollments() is empty, so not enrolled

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> assignmentService.uploadAssignment(submission));
        assertEquals("Student is not enrolled in this course", ex.getMessage());
    }

    @Test
    void gradeAssignment_Success() {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setStudent(student);
        submission.setAssignment(assignment);

        when(submissionRepository.findByStudentUserAccountIdAndAssignmentAssignmentId(200L, 10L))
                .thenReturn(Optional.of(submission));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(submissionRepository.save(any(AssignmentSubmission.class))).thenReturn(submission);

        AssignmentSubmission graded = assignmentService.gradeAssignment(200L, 10L, 85.0);

        assertEquals(85.0, graded.getGrade());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void gradeAssignment_NotInstructor_ThrowsException() {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setStudent(student);
        submission.setAssignment(assignment);

        when(submissionRepository.findByStudentUserAccountIdAndAssignmentAssignmentId(200L, 10L))
                .thenReturn(Optional.of(submission));
        when(userService.getCurrentUser()).thenReturn(student); // student tries to grade

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> assignmentService.gradeAssignment(200L, 10L, 85.0));
        assertEquals("Only course instructor can grade assignments", ex.getMessage());
    }

    @Test
    void getFeedback_Success() {
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setFeedback("Great work!");
        when(submissionRepository.findByStudentUserAccountIdAndAssignmentAssignmentId(200L, 10L))
                .thenReturn(Optional.of(submission));
        when(userService.getCurrentUser()).thenReturn(student);

        String feedback = assignmentService.getFeedback(10L);
        assertEquals("Great work!", feedback);
    }

    @Test
    void getSubscriptions_InstructorSuccess() {
        when(assignmentRepository.findById(10L)).thenReturn(Optional.of(assignment));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(submissionRepository.findByAssignmentAssignmentId(10L)).thenReturn(List.of());

        List<AssignmentSubmission> subs = assignmentService.getSubmissions(10L);
        assertNotNull(subs);
        verify(submissionRepository).findByAssignmentAssignmentId(10L);
    }
}