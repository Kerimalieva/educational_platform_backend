package com.onlinelearning;

import com.onlinelearning.dto.request.CourseRequest;
import com.onlinelearning.dto.response.CourseResponse;
import com.onlinelearning.entity.Course;
import com.onlinelearning.entity.Instructor;
import com.onlinelearning.entity.Student;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.exception.AccessDeniedException;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.CourseRepository;
import com.onlinelearning.service.CourseService;
import com.onlinelearning.service.UserService;
import com.onlinelearning.util.ConvertHelper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private UserService userService;

    @InjectMocks
    private CourseService courseService;

    private Instructor instructor;
    private Student student;
    private Course course;
    private CourseRequest courseRequest;

    @BeforeEach
    void setUp() {
        instructor = new Instructor();
        instructor.setUserAccountId(100L);
        instructor.setFirstName("John");
        instructor.setLastName("Doe");
        instructor.setEmail("john@example.com");

        student = new Student();
        student.setUserAccountId(200L);
        student.setFirstName("Alice");

        course = new Course();
        course.setCourseId(1L);
        course.setCourseName("Spring Boot");
        course.setDescription("Learn Spring Boot");
        course.setDuration(30);
        course.setInstructor(instructor);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        courseRequest = CourseRequest.builder()
                .courseName("New Course")
                .description("Description")
                .duration(30)
                .build();
    }


    @Test
    void createCourse_Success() {
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setCourseId(1L);
            return c;
        });

        CourseResponse response = courseService.createCourse(courseRequest);

        assertNotNull(response);
        assertEquals("New Course", response.getCourseName());
        assertEquals("Description", response.getDescription());
        assertEquals(30, response.getDuration());
        assertEquals(100L, response.getInstructorId());
        assertEquals("John Doe", response.getInstructorName());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_UserNotInstructor_ThrowsAccessDeniedException() {
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> courseService.createCourse(courseRequest));
        assertEquals("Only instructors can create courses", ex.getMessage());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void createCourse_NullRequest_ThrowsException() {

        assertThrows(NullPointerException.class,
                () -> courseService.createCourse(null));
    }


    @Test
    void getCourseById_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourseById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getCourseId());
        assertEquals("Spring Boot", response.getCourseName());
        assertEquals(100L, response.getInstructorId());
    }

    @Test
    void getCourseById_NotFound_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> courseService.getCourseById(99L));
        assertEquals("Course not found with id: 99", ex.getMessage());
    }


    @Test
    void getAllCourses_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> coursePage = new PageImpl<>(List.of(course), pageable, 1);

        when(courseRepository.findAll(pageable)).thenReturn(coursePage);

        Page<CourseResponse> result = courseService.getAllCourses(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Spring Boot", result.getContent().get(0).getCourseName());
    }

    @Test
    void getAllCourses_EmptyPage_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> emptyPage = Page.empty(pageable);

        when(courseRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<CourseResponse> result = courseService.getAllCourses(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }


    @Test
    void getMyCourses_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> coursePage = new PageImpl<>(List.of(course), pageable, 1);

        when(userService.getCurrentUser()).thenReturn(instructor);
        when(courseRepository.findByInstructorUserAccountId(100L, pageable)).thenReturn(coursePage);

        Page<CourseResponse> result = courseService.getMyCourses(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Spring Boot", result.getContent().get(0).getCourseName());
        verify(courseRepository).findByInstructorUserAccountId(100L, pageable);
    }

    @Test
    void getMyCourses_UserIsNotInstructor_ThrowsAccessDeniedException() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> courseService.getMyCourses(pageable));
        assertEquals("Only instructors can view their own courses", ex.getMessage());
        verify(courseRepository, never()).findByInstructorUserAccountId(any(), any());
    }

    @Test
    void getMyCourses_NoCourses_ReturnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> emptyPage = Page.empty(pageable);

        when(userService.getCurrentUser()).thenReturn(instructor);
        when(courseRepository.findByInstructorUserAccountId(100L, pageable)).thenReturn(emptyPage);

        Page<CourseResponse> result = courseService.getMyCourses(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }


    @Test
    void updateCourse_Success_FullUpdate() {
        CourseRequest updateRequest = CourseRequest.builder()
                .courseName("Updated Name")
                .description("Updated Description")
                .duration(45)
                .build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.updateCourse(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Name", response.getCourseName());
        assertEquals("Updated Description", response.getDescription());
        assertEquals(45, response.getDuration());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void updateCourse_Success_PartialUpdate() {
        CourseRequest updateRequest = CourseRequest.builder()
                .courseName("Only Name Updated")
                .duration(45).build();

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        CourseResponse response = courseService.updateCourse(1L, updateRequest);

        assertEquals("Only Name Updated", response.getCourseName());
        assertEquals("Learn Spring Boot", response.getDescription());
        assertEquals(45, response.getDuration());
    }

    @Test
    void updateCourse_NotInstructor_ThrowsAccessDeniedException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> courseService.updateCourse(1L, courseRequest));
        assertEquals("Only course instructor can update the course", ex.getMessage());
        verify(courseRepository, never()).save(any());
    }

    @Test
    void updateCourse_CourseNotFound_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> courseService.updateCourse(99L, courseRequest));
        assertEquals("Course not found with id: 99", ex.getMessage());
    }

    @Test
    void updateCourse_DifferentInstructor_ThrowsAccessDeniedException() {
        Instructor anotherInstructor = new Instructor();
        anotherInstructor.setUserAccountId(999L);

        Course anotherCourse = new Course();
        anotherCourse.setCourseId(2L);
        anotherCourse.setInstructor(anotherInstructor);

        when(courseRepository.findById(2L)).thenReturn(Optional.of(anotherCourse));
        when(userService.getCurrentUser()).thenReturn(instructor);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> courseService.updateCourse(2L, courseRequest));
        assertEquals("Only course instructor can update the course", ex.getMessage());
    }


    @Test
    void deleteCourse_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        doNothing().when(courseRepository).delete(course);

        assertDoesNotThrow(() -> courseService.deleteCourse(1L));
        verify(courseRepository).delete(course);
    }

    @Test
    void deleteCourse_NotInstructor_ThrowsAccessDeniedException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(student);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> courseService.deleteCourse(1L));
        assertEquals("Only course instructor can delete the course", ex.getMessage());
        verify(courseRepository, never()).delete(any());
    }

    @Test
    void deleteCourse_CourseNotFound_ThrowsResourceNotFoundException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> courseService.deleteCourse(99L));
        assertEquals("Course not found with id: 99", ex.getMessage());
    }

    @Test
    void deleteCourse_DifferentInstructor_ThrowsAccessDeniedException() {
        Instructor anotherInstructor = new Instructor();
        anotherInstructor.setUserAccountId(999L);

        Course anotherCourse = new Course();
        anotherCourse.setCourseId(2L);
        anotherCourse.setInstructor(anotherInstructor);

        when(courseRepository.findById(2L)).thenReturn(Optional.of(anotherCourse));
        when(userService.getCurrentUser()).thenReturn(instructor);

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> courseService.deleteCourse(2L));
        assertEquals("Only course instructor can delete the course", ex.getMessage());
    }


    @Test
    void testConvertToCourseResponse() {
        CourseResponse response = ConvertHelper.toCourseResponse(course);
        assertNotNull(response);
        assertEquals(1L, response.getCourseId());
        assertEquals("Spring Boot", response.getCourseName());
        assertEquals(100L, response.getInstructorId());
        assertEquals("John Doe", response.getInstructorName());
    }
}