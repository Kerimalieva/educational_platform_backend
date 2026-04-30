package com.onlinelearning.controller;

import com.onlinelearning.dto.response.*;
import com.onlinelearning.entity.Course;
import com.onlinelearning.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseForAdmin>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(adminService.getAllCourses());
    }

    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long courseId) {
        adminService.deleteCourseByAdmin(courseId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        adminService.deleteLessonByAdmin(lessonId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long assignmentId) {
        adminService.deleteAssignmentByAdmin(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/quizzes/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long quizId) {
        adminService.deleteQuizByAdmin(quizId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        return ResponseEntity.ok(adminService.getStatistics());
    }


    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<LessonResponse>> getCourseLessons(@PathVariable Long courseId) {
        return ResponseEntity.ok(adminService.getLessonsByCourse(courseId));
    }

    @GetMapping("/courses/{courseId}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getCourseAssignments(@PathVariable Long courseId) {
        return ResponseEntity.ok(adminService.getAssignmentsByCourse(courseId));
    }

    @GetMapping("/courses/{courseId}/quizzes")
    public ResponseEntity<List<QuizResponse>> getCourseQuizzes(@PathVariable Long courseId) {
        return ResponseEntity.ok(adminService.getQuizzesByCourse(courseId));
    }

    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<List<QuestionResponse>> getCourseBankQuestions(@PathVariable Long courseId) {
        return ResponseEntity.ok(adminService.getBankQuestionsByCourse(courseId));
    }

    @GetMapping("/courses/{courseId}/content")
    public ResponseEntity<CourseContentResponse> getCourseFullContent(@PathVariable Long courseId) {
        return ResponseEntity.ok(adminService.getCourseContent(courseId));
    }

}