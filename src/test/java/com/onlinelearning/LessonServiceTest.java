package com.onlinelearning;

import com.onlinelearning.dto.LessonDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import com.onlinelearning.service.LessonService;
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
    private LessonDTO lessonDTO;

    @BeforeEach
    void setUp() {
        instructor = new Instructor();
        instructor.setUserAccountId(100L);

        student = new Student();
        student.setUserAccountId(200L);

        course = new Course();
        course.setCourseId(1L);
        course.setInstructor(instructor);

        lesson = new Lesson();
        lesson.setLessonId(10L);
        lesson.setLessonName("First Lesson");
        lesson.setCourse(course);
        lesson.setOTP("123456");

        lessonDTO = new LessonDTO();
        lessonDTO.setLessonName("New Lesson");
        lessonDTO.setCourseId(1L);
    }

    @Test
    void createLesson_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson);

        Lesson result = lessonService.createLesson(lessonDTO);
        assertNotNull(result);
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    void createLesson_NotInstructor_ThrowsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> lessonService.createLesson(lessonDTO));
        assertEquals("Only course instructor can create lessons", ex.getMessage());
    }

    @Test
    void studentEnterLesson_Success() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(true);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(attendanceRepository.findByStudentUserAccountIdAndLessonLessonId(200L, 10L))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        Attendance attendance = lessonService.studentEnterLesson(1L, 10L, "123456");
        assertNotNull(attendance);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void studentEnterLesson_InvalidOTP_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(true);
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> lessonService.studentEnterLesson(1L, 10L, "wrong"));
        assertEquals("Invalid OTP", ex.getMessage());
    }

    @Test
    void studentEnterLesson_NotEnrolled_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(student);
        when(enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(200L, 1L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> lessonService.studentEnterLesson(1L, 10L, "123456"));
        assertEquals("Student is not enrolled in this course", ex.getMessage());
    }

    @Test
    void getLessonAttendances_AsInstructor_Success() {
        when(lessonRepository.findById(10L)).thenReturn(Optional.of(lesson));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(attendanceRepository.findByLessonLessonId(10L)).thenReturn(List.of());

        List<Attendance> list = lessonService.getLessonAttendances(10L);
        assertNotNull(list);
        verify(attendanceRepository).findByLessonLessonId(10L);
    }
}