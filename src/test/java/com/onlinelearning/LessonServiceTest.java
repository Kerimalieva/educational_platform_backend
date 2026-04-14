package com.onlinelearning;

import com.onlinelearning.dto.request.LessonRequest;
import com.onlinelearning.dto.response.AttendanceResponse;
import com.onlinelearning.dto.response.LessonResponse;
import com.onlinelearning.entity.*;
import com.onlinelearning.exception.AccessDeniedException;
import com.onlinelearning.exception.BusinessException;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.*;
import com.onlinelearning.service.LessonService;
import com.onlinelearning.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock private LessonRepository lessonRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private UserService userService;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private LessonService lessonService;

    private Instructor instructor;
    private Student student;
    private Course course;
    private Lesson lesson;
    private LessonRequest lessonRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        instructor = new Instructor();
        instructor.setUserAccountId(100L);
        instructor.setFirstName("John");

        student = new Student();
        student.setUserAccountId(200L);
        student.setFirstName("Alice");
        student.setLastName("Brown");

        course = new Course();
        course.setCourseId(1L);
        course.setCourseName("Spring Boot");
        course.setInstructor(instructor);

        lesson = new Lesson();
        lesson.setLessonId(10L);
        lesson.setLessonName("First Lesson");
        lesson.setLessonDescription("Intro");
        lesson.setLessonOrder(1);
        lesson.setContent("Content");
        lesson.setCourse(course);
        lesson.setOTP("123456");

        lessonRequest = LessonRequest.builder()
                .lessonName("New Lesson")
                .lessonDescription("Desc")
                .lessonOrder(1)
                .content("Content")
                .courseId(1L)
                .build();

        pageable = PageRequest.of(0, 10);
    }


    @Test
    void createLesson_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        LessonResponse response = lessonService.createLesson(lessonRequest);

        assertNotNull(response);
        assertEquals("First Lesson", response.getLessonName());
        assertEquals(10L, response.getLessonId());
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void createLesson_CourseNotFound_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> lessonService.createLesson(lessonRequest));
        assertEquals("Course not found with id: 1", ex.getMessage());
    }

    @Test
    void createLesson_NotInstructor_ThrowsAccessDeniedException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.createLesson(lessonRequest));
        assertEquals("Only course instructor can create lessons", ex.getMessage());
    }


    @Test
    void getLessonsByCourse_AsInstructor_Success() {
        Page<Lesson> lessonPage = new PageImpl<>(List.of(lesson), pageable, 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(lessonRepository.findByCourseCourseIdOrderByLessonOrderAsc(1L, pageable)).thenReturn(lessonPage);

        Page<LessonResponse> response = lessonService.getLessonsByCourse(1L, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("First Lesson", response.getContent().get(0).getLessonName());
    }

    @Test
    void getLessonsByCourse_AsEnrolledStudent_Success() {
        Page<Lesson> lessonPage = new PageImpl<>(List.of(lesson), pageable, 1);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(true);
        when(lessonRepository.findByCourseCourseIdOrderByLessonOrderAsc(1L, pageable)).thenReturn(lessonPage);

        Page<LessonResponse> response = lessonService.getLessonsByCourse(1L, pageable);

        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getLessonsByCourse_NotEnrolledStudent_ThrowsAccessDeniedException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.getLessonsByCourse(1L, pageable));
        assertEquals("You are not enrolled in this course or not the instructor", ex.getMessage());
    }


    @Test
    void getLessonById_Success() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        LessonResponse response = lessonService.getLessonById(10L);

        assertEquals(10L, response.getLessonId());
        assertEquals("First Lesson", response.getLessonName());
    }

    @Test
    void getLessonById_NotFound_ThrowsResourceNotFoundException() {
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> lessonService.getLessonById(99L));
        assertEquals("Lesson not found with id: 99", ex.getMessage());
    }


    @Test
    void updateLesson_Success() {
        LessonRequest updateRequest = LessonRequest.builder()
                .lessonName("Updated Name")
                .lessonDescription("New Desc")
                .build();

        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        LessonResponse response = lessonService.updateLesson(10L, updateRequest);

        assertEquals("Updated Name", response.getLessonName());
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void updateLesson_NotInstructor_ThrowsAccessDeniedException() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.updateLesson(10L, lessonRequest));
        assertEquals("Only course instructor can update lessons", ex.getMessage());
    }


    @Test
    void deleteLesson_Success() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(instructor);
        doNothing().when(lessonRepository).delete(lesson);

        assertDoesNotThrow(() -> lessonService.deleteLesson(10L));
        verify(lessonRepository).delete(lesson);
    }

    @Test
    void deleteLesson_NotInstructor_ThrowsAccessDeniedException() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.deleteLesson(10L));
        assertEquals("Only course instructor can delete lessons", ex.getMessage());
    }


    @Test
    void studentEnterLesson_Success() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(true);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(attendanceRepository.findByStudentUserAccountIdAndLessonLessonId(200L, 10L))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        AttendanceResponse response = lessonService.studentEnterLesson(1L, 10L, "123456");

        assertNotNull(response);
        assertEquals(200L, response.getStudentId());
        assertEquals(10L, response.getLessonId());
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void studentEnterLesson_NotStudent_ThrowsAccessDeniedException() {
        when(userService.getCurrentUser()).thenReturn(instructor);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.studentEnterLesson(1L, 10L, "123456"));
        assertEquals("Only students can enter lessons", ex.getMessage());
    }

    @Test
    void studentEnterLesson_NotEnrolled_ThrowsAccessDeniedException() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(false);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.studentEnterLesson(1L, 10L, "123456"));
        assertEquals("Student is not enrolled in this course", ex.getMessage());
    }

    @Test
    void studentEnterLesson_LessonNotFound_ThrowsResourceNotFoundException() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(true);
        when(lessonRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> lessonService.studentEnterLesson(1L, 99L, "123456"));
        assertEquals("Lesson not found with id: 99", ex.getMessage());
    }

    @Test
    void studentEnterLesson_InvalidOTP_ThrowsBusinessException() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(true);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> lessonService.studentEnterLesson(1L, 10L, "wrong"));
        assertEquals("Invalid OTP", ex.getMessage());
        assertEquals("INVALID_OTP", ex.getErrorCode());
    }

    @Test
    void studentEnterLesson_DuplicateAttendance_ThrowsBusinessException() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(true);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(attendanceRepository.findByStudentUserAccountIdAndLessonLessonId(200L, 10L))
                .thenReturn(Optional.of(new Attendance()));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> lessonService.studentEnterLesson(1L, 10L, "123456"));
        assertEquals("Attendance already marked for this lesson", ex.getMessage());
        assertEquals("DUPLICATE_ATTENDANCE", ex.getErrorCode());
    }


    @Test
    void getLessonAttendances_AsInstructor_Success() {
        Attendance attendance = new Attendance(student, lesson);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(attendanceRepository.findByLessonLessonId(10L)).thenReturn(List.of(attendance));

        List<AttendanceResponse> responses = lessonService.getLessonAttendances(10L);

        assertEquals(1, responses.size());
        assertEquals("Alice Brown", responses.get(0).getStudentName());
    }

    @Test
    void getLessonAttendances_NotInstructor_ThrowsAccessDeniedException() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.getLessonAttendances(10L));
        assertEquals("Only course instructor can view attendances", ex.getMessage());
    }


    @Test
    void regenerateOtp_Success() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        String newOtp = lessonService.regenerateOtp(10L);

        assertNotNull(newOtp);
        assertEquals(6, newOtp.length());
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void regenerateOtp_NotInstructor_ThrowsAccessDeniedException() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> lessonService.regenerateOtp(10L));
        assertEquals("Only course instructor can regenerate OTP", ex.getMessage());
    }
}