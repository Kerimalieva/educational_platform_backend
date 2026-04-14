package com.onlinelearning.controller;

import com.onlinelearning.dto.request.EnrollmentRequest;
import com.onlinelearning.dto.response.EnrollmentResponse;
import com.onlinelearning.service.EnrollmentService;
import com.onlinelearning.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserService userService;

    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponse> enrollStudent(@Valid @RequestBody EnrollmentRequest request) {
        log.debug("REST request to enroll student {} into course {}", request.getStudentId(), request.getCourseId());
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<com.onlinelearning.entity.Course>> getMyCourses() {
        log.debug("REST request to get my courses");
        com.onlinelearning.entity.Student student = (com.onlinelearning.entity.Student) userService.getCurrentUser();
        List<com.onlinelearning.entity.Course> courses = enrollmentService.getCoursesByStudent(student.getUserAccountId());
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/enroll/current/{courseId}")
    public ResponseEntity<EnrollmentResponse> enrollCurrentStudent(@PathVariable Long courseId) {
        log.debug("REST request to enroll current student into course {}", courseId);
        com.onlinelearning.entity.Student student = (com.onlinelearning.entity.Student) userService.getCurrentUser();
        EnrollmentRequest request = EnrollmentRequest.builder()
                .studentId(student.getUserAccountId())
                .courseId(courseId)
                .build();
        EnrollmentResponse response = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/unenroll/{courseId}")
    public ResponseEntity<Void> unenrollCurrentStudent(@PathVariable Long courseId) {
        log.debug("REST request to unenroll current student from course {}", courseId);
        com.onlinelearning.entity.Student student = (com.onlinelearning.entity.Student) userService.getCurrentUser();
        enrollmentService.unenrollStudent(student.getUserAccountId(), courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        log.debug("REST request to get enrollments for course {}", courseId);
        List<EnrollmentResponse> responses = enrollmentService.getEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/remove/{studentId}/{courseId}")
    public ResponseEntity<Void> removeEnrollment(@PathVariable Long studentId, @PathVariable Long courseId) {
        log.debug("REST request to remove student {} from course {}", studentId, courseId);
        enrollmentService.unenrollStudent(studentId, courseId);
        return ResponseEntity.noContent().build();
    }
}