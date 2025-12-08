package com.onlinelearning.controller;

import com.onlinelearning.dto.LessonDTO;
import com.onlinelearning.entity.Attendance;
import com.onlinelearning.entity.Lesson;
import com.onlinelearning.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/lesson")
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @PostMapping("/add_lesson")
    public ResponseEntity<Lesson> addLesson(@Valid @RequestBody LessonDTO lessonDTO) {
        Lesson lesson = lessonService.createLesson(lessonDTO);
        return ResponseEntity.ok(lesson);
    }

    @GetMapping("/get_all_lessons/{courseId}")
    public ResponseEntity<List<LessonDTO>> getAllLessons(@PathVariable Long courseId) {
        List<LessonDTO> lessons = lessonService.getAllLessonsByCourse(courseId);
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/lesson_id/{lessonId}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Long lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(lesson);
    }

    @PutMapping("/update/lesson_id/{lessonId}")
    public ResponseEntity<Lesson> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonDTO lessonDTO) {

        Lesson updatedLesson = lessonService.updateLesson(lessonId, lessonDTO);
        return ResponseEntity.ok(updatedLesson);
    }

    @DeleteMapping("/delete/lesson_id/{lessonId}/course_id/{courseId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long lessonId,
            @PathVariable Long courseId) {

        lessonService.deleteLesson(lessonId, courseId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/student_enter_lesson/course_id/{courseId}/lesson_id/{lessonId}/otp/{otp}")
    public ResponseEntity<Attendance> studentEnterLesson(
            @PathVariable Long courseId,
            @PathVariable Long lessonId,
            @PathVariable String otp) {

        Attendance attendance = lessonService.studentEnterLesson(courseId, lessonId, otp);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/attendances/{lessonId}")
    public ResponseEntity<List<Attendance>> getLessonAttendances(@PathVariable Long lessonId) {
        List<Attendance> attendances = lessonService.getLessonAttendances(lessonId);
        return ResponseEntity.ok(attendances);
    }
}