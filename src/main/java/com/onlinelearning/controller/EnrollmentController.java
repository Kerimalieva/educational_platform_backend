package com.onlinelearning.controller;

import com.onlinelearning.dto.EnrollmentDTO;
import com.onlinelearning.entity.Course;
import com.onlinelearning.entity.Enrollment;
import com.onlinelearning.entity.Student;
import com.onlinelearning.service.EnrollmentService;
import com.onlinelearning.service.UserService;
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

    @Autowired
    private UserService userService;

    // === НОВЫЕ ЭНДПОИНТЫ ДЛЯ ФРОНТА ===

    // Получить курсы, на которые записан текущий студент
    @GetMapping("/my-courses")
    public ResponseEntity<List<Course>> getMyCourses() {
        Student student = (Student) userService.getCurrentUser();
        List<Course> courses = enrollmentService.getCoursesByStudent(student.getUserAccountId());
        return ResponseEntity.ok(courses);
    }

    // Записаться на курс (текущий студент)
    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<?> enrollStudent(@PathVariable Long courseId) {
        Student student = (Student) userService.getCurrentUser();
        Enrollment enrollment = enrollmentService.enrollStudent(student.getUserAccountId(), courseId);
        return ResponseEntity.ok(enrollment);
    }

    // Отписаться от курса (текущий студент)
    @DeleteMapping("/unenroll/{courseId}")
    public ResponseEntity<?> unenrollStudent(@PathVariable Long courseId) {
        Student student = (Student) userService.getCurrentUser();
        enrollmentService.unenrollStudent(student.getUserAccountId(), courseId);
        return ResponseEntity.ok().build();
    }

    // === СТАРЫЕ ЭНДПОИНТЫ (можно оставить для совместимости) ===

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