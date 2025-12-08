package com.onlinelearning.controller;

import com.onlinelearning.dto.EnrollmentDTO;
import com.onlinelearning.entity.Enrollment;
import com.onlinelearning.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/enrollment")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    public ResponseEntity<Enrollment> enrollStudent(@Valid @RequestBody EnrollmentDTO enrollmentDTO) {
        Enrollment enrollment = enrollmentService.enrollStudent(enrollmentDTO);
        return ResponseEntity.ok(enrollment);
    }

    @GetMapping("/view_enrolled_students/{courseId}")
    public ResponseEntity<List<EnrollmentDTO>> viewEnrolledStudents(@PathVariable Long courseId) {
        List<EnrollmentDTO> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(enrollments);
    }

    @DeleteMapping("/remove_enrolled_student/student_id/{studentId}/course_id/{courseId}")
    public ResponseEntity<Void> removeEnrolledStudent(
            @PathVariable Long studentId,
            @PathVariable Long courseId) {

        enrollmentService.removeEnrollment(studentId, courseId);
        return ResponseEntity.ok().build();
    }
}