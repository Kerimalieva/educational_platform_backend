package com.onlinelearning;

import com.onlinelearning.dto.request.EnrollmentRequest;
import com.onlinelearning.dto.response.EnrollmentResponse;
import com.onlinelearning.entity.*;
import com.onlinelearning.exception.AccessDeniedException;
import com.onlinelearning.exception.BusinessException;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.*;
import com.onlinelearning.service.EnrollmentService;
import com.onlinelearning.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserService userService;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Instructor instructor;
    private Course course;
    private Enrollment enrollment;
    private EnrollmentRequest request;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setUserAccountId(1L);
        student.setFirstName("Alice");
        student.setLastName("Smith");
        student.setEmail("alice@test.com");

        instructor = new Instructor();
        instructor.setUserAccountId(100L);
        instructor.setFirstName("John");
        instructor.setLastName("Doe");

        course = new Course();
        course.setCourseId(10L);
        course.setCourseName("Java Course");
        course.setInstructor(instructor);

        enrollment = new Enrollment(student, course);
        enrollment.setEnrollmentId(5L);

        request = EnrollmentRequest.builder()
                .studentId(1L)
                .courseId(10L)
                .build();
    }


    @Test
    void enrollStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(1L, 10L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);
        when(notificationRepository.save(any(Notification.class))).thenReturn(new Notification());

        EnrollmentResponse response = enrollmentService.enrollStudent(request);

        assertNotNull(response);
        assertEquals(5L, response.getEnrollmentId());
        assertEquals(1L, response.getStudentId());
        assertEquals("Alice Smith", response.getStudentName());
        assertEquals(10L, response.getCourseId());
        assertEquals("Java Course", response.getCourseName());
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void enrollStudent_StudentNotFound_ThrowsResourceNotFoundException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.enrollStudent(request));
        assertEquals("Student not found with id: 1", ex.getMessage());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void enrollStudent_CourseNotFound_ThrowsResourceNotFoundException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.enrollStudent(request));
        assertEquals("Course not found with id: 10", ex.getMessage());
    }

    @Test
    void enrollStudent_AlreadyEnrolled_ThrowsBusinessException() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(1L, 10L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> enrollmentService.enrollStudent(request));
        assertEquals("Already enrolled in this course", ex.getMessage());
        assertEquals("DUPLICATE_ENROLLMENT", ex.getErrorCode());
        verify(enrollmentRepository, never()).save(any());
    }


    @Test
    void getEnrollmentsByStudent_AsStudentOwner_Success() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.findByStudentUserAccountId(1L)).thenReturn(List.of(enrollment));

        List<EnrollmentResponse> responses = enrollmentService.getEnrollmentsByStudent(1L);

        assertEquals(1, responses.size());
        assertEquals("Alice Smith", responses.get(0).getStudentName());
        verify(enrollmentRepository).findByStudentUserAccountId(1L);
    }

    @Test
    void getEnrollmentsByStudent_AsInstructor_Success() {
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(enrollmentRepository.findByStudentUserAccountId(1L)).thenReturn(List.of(enrollment));

        List<EnrollmentResponse> responses = enrollmentService.getEnrollmentsByStudent(1L);

        assertEquals(1, responses.size());
        verify(enrollmentRepository).findByStudentUserAccountId(1L);
    }

    @Test
    void getEnrollmentsByStudent_AsOtherStudent_ThrowsAccessDeniedException() {
        Student otherStudent = new Student();
        otherStudent.setUserAccountId(999L);
        when(userService.getCurrentUser()).thenReturn(otherStudent);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> enrollmentService.getEnrollmentsByStudent(1L));
        assertEquals("You can only view your own enrollments", ex.getMessage());
        verify(enrollmentRepository, never()).findByStudentUserAccountId(any());
    }


    @Test
    void getEnrollmentsByCourse_AsInstructor_Success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(enrollmentRepository.findByCourseCourseId(10L)).thenReturn(List.of(enrollment));

        List<EnrollmentResponse> responses = enrollmentService.getEnrollmentsByCourse(10L);

        assertEquals(1, responses.size());
        assertEquals("Alice Smith", responses.get(0).getStudentName());
        verify(enrollmentRepository).findByCourseCourseId(10L);
    }

    @Test
    void getEnrollmentsByCourse_NotInstructor_ThrowsAccessDeniedException() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> enrollmentService.getEnrollmentsByCourse(10L));
        assertEquals("Only course instructor can view enrolled students", ex.getMessage());
    }

    @Test
    void getEnrollmentsByCourse_CourseNotFound_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.getEnrollmentsByCourse(99L));
        assertEquals("Course not found with id: 99", ex.getMessage());
    }


    @Test
    void unenrollStudent_AsInstructor_Success() {
        when(enrollmentRepository.findByStudentUserAccountIdAndCourseCourseId(1L, 10L))
                .thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUser()).thenReturn(instructor);

        assertDoesNotThrow(() -> enrollmentService.unenrollStudent(1L, 10L));
        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void unenrollStudent_AsStudentOwner_Success() {
        when(enrollmentRepository.findByStudentUserAccountIdAndCourseCourseId(1L, 10L))
                .thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUser()).thenReturn(student);

        assertDoesNotThrow(() -> enrollmentService.unenrollStudent(1L, 10L));
        verify(enrollmentRepository).delete(enrollment);
    }

    @Test
    void unenrollStudent_AsOtherStudent_ThrowsAccessDeniedException() {
        Student otherStudent = new Student();
        otherStudent.setUserAccountId(999L);
        when(enrollmentRepository.findByStudentUserAccountIdAndCourseCourseId(1L, 10L))
                .thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUser()).thenReturn(otherStudent);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> enrollmentService.unenrollStudent(1L, 10L));
        assertEquals("Only course instructor or the student himself can unenroll", ex.getMessage());
        verify(enrollmentRepository, never()).delete(any());
    }

    @Test
    void unenrollStudent_EnrollmentNotFound_ThrowsResourceNotFoundException() {
        when(enrollmentRepository.findByStudentUserAccountIdAndCourseCourseId(1L, 10L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> enrollmentService.unenrollStudent(1L, 10L));
        assertTrue(ex.getMessage().contains("Enrollment not found"));
    }


    @Test
    void getCoursesByStudent_Success() {
        when(enrollmentRepository.findByStudentUserAccountId(1L)).thenReturn(List.of(enrollment));

        List<Course> courses = enrollmentService.getCoursesByStudent(1L);

        assertEquals(1, courses.size());
        assertEquals("Java Course", courses.get(0).getCourseName());
    }
}