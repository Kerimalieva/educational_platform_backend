package com.onlinelearning.controller;

import com.onlinelearning.dto.request.CourseRequest;
import com.onlinelearning.dto.response.CourseResponse;
import com.onlinelearning.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/add_course")
    public ResponseEntity<CourseResponse> addCourse(@Valid @RequestBody CourseRequest request) {
        log.debug("REST request to add course: {}", request.getCourseName());
        CourseResponse response = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/course_id/{id}")
    public ResponseEntity<CourseResponse> getCourseById(@PathVariable Long id) {
        log.debug("REST request to get course by id: {}", id);
        CourseResponse response = courseService.getCourseById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all_courses")
    public ResponseEntity<Page<CourseResponse>> getAllCourses(
            @PageableDefault(size = 10, sort = "courseId", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("REST request to get all courses, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<CourseResponse> page = courseService.getAllCourses(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/my-courses")
    public ResponseEntity<Page<CourseResponse>> getMyCourses(
            @PageableDefault(size = 10, sort = "courseId", direction = Sort.Direction.DESC) Pageable pageable) {
        log.debug("REST request to get my courses");
        Page<CourseResponse> page = courseService.getMyCourses(pageable);
        return ResponseEntity.ok(page);
    }

    @PutMapping("/update/course_id/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseRequest request) {
        log.debug("REST request to update course id: {}", courseId);
        CourseResponse response = courseService.updateCourse(courseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/course_id/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        log.debug("REST request to delete course id: {}", courseId);
        courseService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }
}