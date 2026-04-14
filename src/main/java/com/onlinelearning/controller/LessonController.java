package com.onlinelearning.controller;

import com.onlinelearning.dto.request.LessonRequest;
import com.onlinelearning.dto.response.AttendanceResponse;
import com.onlinelearning.dto.response.LessonResponse;
import com.onlinelearning.service.LessonService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/lesson")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/add_lesson")
    public ResponseEntity<LessonResponse> addLesson(@Valid @RequestBody LessonRequest request) {
        log.debug("REST request to add lesson: {}", request.getLessonName());
        LessonResponse response = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/get_all_lessons/{courseId}")
    public ResponseEntity<Page<LessonResponse>> getAllLessons(
            @PathVariable Long courseId,
            @PageableDefault(size = 20, sort = "lessonOrder", direction = Sort.Direction.ASC) Pageable pageable) {
        log.debug("REST request to get lessons for courseId: {}", courseId);
        Page<LessonResponse> page = lessonService.getLessonsByCourse(courseId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/lesson_id/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Long lessonId) {
        log.debug("REST request to get lesson by id: {}", lessonId);
        LessonResponse response = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/lesson_id/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonRequest request) {
        log.debug("REST request to update lesson id: {}", lessonId);
        LessonResponse response = lessonService.updateLesson(lessonId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/lesson_id/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        log.debug("REST request to delete lesson id: {}", lessonId);
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/student_enter_lesson/course_id/{courseId}/lesson_id/{lessonId}/otp/{otp}")
    public ResponseEntity<AttendanceResponse> studentEnterLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable String otp) {
        log.debug("REST request to enter lesson: courseId={}, lessonId={}", courseId, lessonId);
        AttendanceResponse response = lessonService.studentEnterLesson(courseId, lessonId, otp);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attendances/{lessonId}")
    public ResponseEntity<List<AttendanceResponse>> getLessonAttendances(@PathVariable Long lessonId) {
        log.debug("REST request to get attendances for lessonId: {}", lessonId);
        List<AttendanceResponse> responses = lessonService.getLessonAttendances(lessonId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/regenerate_otp/{lessonId}")
    public ResponseEntity<String> regenerateOtp(@PathVariable Long lessonId) {
        log.debug("REST request to regenerate OTP for lessonId: {}", lessonId);
        String newOtp = lessonService.regenerateOtp(lessonId);
        return ResponseEntity.ok(newOtp);
    }
}