package com.onlinelearning.service;

import com.onlinelearning.dto.CourseDTO;
import com.onlinelearning.entity.Course;
import com.onlinelearning.entity.Instructor;
import com.onlinelearning.entity.Media;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.repository.CourseRepository;
import com.onlinelearning.repository.InstructorRepository;
import com.onlinelearning.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private UserService userService;

    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public CourseService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Transactional
    public Course createCourse(CourseDTO courseDTO) {
        UserAccount currentUser = userService.getCurrentUser();

        // Check if user is instructor
        if (!(currentUser instanceof Instructor)) {
            throw new RuntimeException("Only instructors can create courses");
        }

        Instructor instructor = (Instructor) currentUser;

        Course course = new Course();
        course.setCourseName(courseDTO.getCourseName());
        course.setDescription(courseDTO.getDescription());
        course.setDuration(courseDTO.getDuration());
        course.setInstructor(instructor);

        return courseRepository.save(course);
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CourseDTO> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorUserAccountId(instructorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Course updateCourse(Long courseId, CourseDTO courseDTO) {
        Course course = getCourseById(courseId);

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can update the course");
        }

        if (courseDTO.getCourseName() != null) {
            course.setCourseName(courseDTO.getCourseName());
        }

        if (courseDTO.getDescription() != null) {
            course.setDescription(courseDTO.getDescription());
        }

        if (courseDTO.getDuration() != null) {
            course.setDuration(courseDTO.getDuration());
        }

        course.setUpdatedAt(java.time.LocalDateTime.now());

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = getCourseById(courseId);

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can delete the course");
        }

        courseRepository.delete(course);
    }

    @Transactional
    public Media uploadMedia(Long courseId, MultipartFile file) throws IOException {
        Course course = getCourseById(courseId);

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can upload media");
        }

        // Normalize file name
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Copy file to the target location
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation);

        // Create media entity
        Media media = new Media();
        media.setFileName(file.getOriginalFilename());
        media.setFilePath(targetLocation.toString());
        media.setFileType(file.getContentType());
        media.setFileSize(file.getSize());
        media.setCourse(course);

        return mediaRepository.save(media);
    }

    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setDuration(course.getDuration());
        dto.setInstructorId(course.getInstructor().getUserAccountId());
        dto.setInstructorName(course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName());
        return dto;
    }
}