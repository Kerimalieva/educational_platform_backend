package com.onlinelearning;

import com.onlinelearning.dto.CourseDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.CourseRepository;
import com.onlinelearning.repository.InstructorRepository;
import com.onlinelearning.repository.MediaRepository;
import com.onlinelearning.service.CourseService;
import com.onlinelearning.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private MediaRepository mediaRepository;
    @Mock private UserService userService;

    @InjectMocks
    private CourseService courseService;

    private Instructor instructor;
    private Course course;
    private CourseDTO courseDTO;

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

        courseDTO = new CourseDTO();
        courseDTO.setCourseName("New Course");
        courseDTO.setDescription("Description");
        courseDTO.setDuration(30);
    }

    @Test
    void createCourse_Success() {
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        Course result = courseService.createCourse(courseDTO);

        assertNotNull(result);
        assertEquals("Spring Boot", result.getCourseName());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_UserNotInstructor_ThrowsException() {
        Student student = new Student();
        when(userService.getCurrentUser()).thenReturn(student);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.createCourse(courseDTO));
        assertEquals("Only instructors can create courses", ex.getMessage());
    }

    @Test
    void getCourseById_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        Course found = courseService.getCourseById(1L);
        assertEquals(1L, found.getCourseId());
    }

    @Test
    void getCourseById_NotFound_ThrowsException() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.getCourseById(99L));
        assertEquals("Course not found", ex.getMessage());
    }

    @Test
    void updateCourse_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(courseRepository.save(any(Course.class))).thenReturn(course);

        Course updated = courseService.updateCourse(1L, courseDTO);
        assertEquals("New Course", updated.getCourseName());
    }

    @Test
    void updateCourse_NotInstructor_ThrowsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(new Student());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.updateCourse(1L, courseDTO));
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
    void uploadMedia_Success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getInputStream()).thenReturn(mock(java.io.InputStream.class));
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(1024L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(mediaRepository.save(any(Media.class))).thenAnswer(inv -> inv.getArgument(0));

        Media media = courseService.uploadMedia(1L, file);
        assertNotNull(media);
        assertEquals("test.pdf", media.getFileName());
        // Clean up created directory if needed
    }
}