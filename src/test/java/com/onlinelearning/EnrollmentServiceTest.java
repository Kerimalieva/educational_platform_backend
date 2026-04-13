package com.onlinelearning;

import com.onlinelearning.dto.EnrollmentDTO;
import com.onlinelearning.entity.*;
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
    private Course course;
    private Instructor instructor;
    private Enrollment enrollment;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setUserAccountId(1L);
        student.setFirstName("Alice");
        student.setLastName("Smith");

        instructor = new Instructor();
        instructor.setUserAccountId(100L);
        instructor.setFirstName("John");

        course = new Course();
        course.setCourseId(10L);
        course.setCourseName("Java Course");
        course.setInstructor(instructor);

        enrollment = new Enrollment(student, course);
        enrollment.setEnrollmentId(5L);
    }
    @Test
    void enrollStudent_Success() {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setStudentId(1L);
        dto.setCourseId(10L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(1L, 10L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        Enrollment result = enrollmentService.enrollStudent(dto);
        assertNotNull(result);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void enrollStudent_AlreadyEnrolled_ThrowsException() {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setStudentId(1L);
        dto.setCourseId(10L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(1L, 10L)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> enrollmentService.enrollStudent(dto));
        assertEquals("Student is already enrolled in this course", ex.getMessage());
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void getEnrollmentsByCourse_AsInstructor_Success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(enrollmentRepository.findByCourseCourseId(10L)).thenReturn(List.of(enrollment));

        List<EnrollmentDTO> result = enrollmentService.getEnrollmentsByCourse(10L);
        assertEquals(1, result.size());
        assertEquals("Alice Smith", result.get(0).getStudentName());
    }

    @Test
    void getEnrollmentsByCourse_NotInstructor_ThrowsException() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> enrollmentService.getEnrollmentsByCourse(10L));
        assertEquals("Only course instructor can view enrolled students", ex.getMessage());
    }

    @Test
    void removeEnrollment_Success() {
        when(enrollmentRepository.findByStudentUserAccountIdAndCourseCourseId(1L, 10L))
                .thenReturn(Optional.of(enrollment));
        when(userService.getCurrentUser()).thenReturn(instructor);

        assertDoesNotThrow(() -> enrollmentService.removeEnrollment(1L, 10L));
        verify(enrollmentRepository).delete(enrollment);
    }
}