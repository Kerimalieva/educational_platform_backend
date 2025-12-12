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

    // ==================== CREATE ====================
    @PostMapping("/add_lesson")
    public ResponseEntity<LessonDTO> addLesson(@Valid @RequestBody LessonDTO lessonDTO) {
        Lesson lesson = lessonService.createLesson(lessonDTO);
        LessonDTO responseDTO = convertToDTO(lesson);
        return ResponseEntity.ok(responseDTO);
    }

    // ==================== READ ====================
    @GetMapping("/get_all_lessons/{courseId}")
    public ResponseEntity<List<LessonDTO>> getAllLessons(@PathVariable Long courseId) {
        List<LessonDTO> lessons = lessonService.getAllLessonsByCourse(courseId);
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/lesson_id/{lessonId}")
    public ResponseEntity<LessonDTO> getLessonById(@PathVariable Long lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId);
        LessonDTO dto = convertToDTO(lesson);
        return ResponseEntity.ok(dto);
    }

    // ==================== UPDATE ====================
    @PutMapping("/update/lesson_id/{lessonId}")
    public ResponseEntity<LessonDTO> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonDTO lessonDTO) {

        Lesson updatedLesson = lessonService.updateLesson(lessonId, lessonDTO);
        LessonDTO responseDTO = convertToDTO(updatedLesson);
        return ResponseEntity.ok(responseDTO);
    }

    // ==================== DELETE ====================
    @DeleteMapping("/delete/lesson_id/{lessonId}/course_id/{courseId}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable Long lessonId,
            @PathVariable Long courseId) {

        lessonService.deleteLesson(lessonId, courseId);
        return ResponseEntity.ok().build();
    }

    // ==================== STUDENT ATTENDANCE ====================
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

    // ==================== УТИЛИТА: конвертация Lesson → LessonDTO ====================
    private LessonDTO convertToDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setLessonId(lesson.getLessonId());
        dto.setLessonName(lesson.getLessonName());
        dto.setLessonDescription(lesson.getLessonDescription());
        dto.setLessonOrder(lesson.getLessonOrder());
        dto.setContent(lesson.getContent());
        dto.setYoutubeUrl(lesson.getYoutubeUrl());  // ← Важно для видео
        dto.setCourseId(lesson.getCourse().getCourseId());
        dto.setCourseName(lesson.getCourse().getCourseName());
        return dto;
    }
}