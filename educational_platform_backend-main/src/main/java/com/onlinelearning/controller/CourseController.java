package com.onlinelearning.controller;

import com.onlinelearning.dto.CourseDTO;
import com.onlinelearning.entity.Course;
import com.onlinelearning.entity.Media;
import com.onlinelearning.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping("/add_course")
    public ResponseEntity<CourseDTO> addCourse(@Valid @RequestBody CourseDTO courseDTO) {
        Course course = courseService.createCourse(courseDTO);

        // Конвертируем в DTO для ответа
        CourseDTO responseDTO = new CourseDTO();
        responseDTO.setCourseId(course.getCourseId());
        responseDTO.setCourseName(course.getCourseName());
        responseDTO.setDescription(course.getDescription());
        responseDTO.setDuration(course.getDuration());
        responseDTO.setInstructorId(course.getInstructor().getUserAccountId());
        if (course.getInstructor().getFirstName() != null && course.getInstructor().getLastName() != null) {
            responseDTO.setInstructorName(
                    course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName()
            );
        } else {
            responseDTO.setInstructorName("Instructor");
        }

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/course_id/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);

        CourseDTO dto = new CourseDTO();
        dto.setCourseId(course.getCourseId());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setDuration(course.getDuration());
        dto.setInstructorId(course.getInstructor().getUserAccountId());
        if (course.getInstructor().getFirstName() != null && course.getInstructor().getLastName() != null) {
            dto.setInstructorName(
                    course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName()
            );
        } else {
            dto.setInstructorName("Instructor");
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/all_courses")
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @PutMapping("/update/course_id/{courseId}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseDTO courseDTO) {

        Course updatedCourse = courseService.updateCourse(courseId, courseDTO);

        CourseDTO responseDTO = new CourseDTO();
        responseDTO.setCourseId(updatedCourse.getCourseId());
        responseDTO.setCourseName(updatedCourse.getCourseName());
        responseDTO.setDescription(updatedCourse.getDescription());
        responseDTO.setDuration(updatedCourse.getDuration());
        responseDTO.setInstructorId(updatedCourse.getInstructor().getUserAccountId());
        if (updatedCourse.getInstructor().getFirstName() != null && updatedCourse.getInstructor().getLastName() != null) {
            responseDTO.setInstructorName(
                    updatedCourse.getInstructor().getFirstName() + " " +
                            updatedCourse.getInstructor().getLastName()
            );
        } else {
            responseDTO.setInstructorName("Instructor");
        }

        return ResponseEntity.ok(responseDTO);
    }

    @DeleteMapping("/delete/course_id/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        courseService.deleteCourse(courseId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload_media/{courseId}")
    public ResponseEntity<Media> uploadMedia(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file) throws IOException {

        Media media = courseService.uploadMedia(courseId, file);
        return ResponseEntity.ok(media);
    }
}