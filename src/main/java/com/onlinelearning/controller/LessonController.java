package com.onlinelearning.controller;

import com.onlinelearning.dto.LessonDTO;
import com.onlinelearning.entity.Attendance;
import com.onlinelearning.entity.Lesson;
import com.onlinelearning.service.LessonService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/lesson")
public class LessonController {

    private static final Logger log = LoggerFactory.getLogger(LessonController.class);

    @Autowired
    private LessonService lessonService;

    // Директория для сохранения загруженных файлов (можно вынести в properties)
    private final String uploadDir = "uploads";

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

    /**
     * Загрузка файла (изображение, PDF, MP4) для использования в контенте урока.
     * Файл сохраняется в папку uploads/, возвращается URL для вставки.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("=== UPLOAD CALLED ===");
        log.info("File name: {}", file.getOriginalFilename());
        log.info("File size: {} bytes", file.getSize());
        log.info("Content type: {}", file.getContentType());

        // 1. Проверка типа файла
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.startsWith("image/") ||
                        contentType.equals("video/mp4") ||
                        contentType.equals("application/pdf"))) {
            log.warn("Unsupported file type: {}", contentType);
            return ResponseEntity.badRequest().body("Unsupported file type. Allowed: images, MP4, PDF");
        }

        // 2. Проверка размера (опционально, можно положиться на глобальные лимиты)
        if (file.getSize() > 100 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File size exceeds 100 MB");
        }

        try {
            // 3. Создать директорию, если не существует
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            }

            // 4. Сгенерировать уникальное имя файла (сохраняем расширение)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(newFileName);

            // 5. Сохранить файл на диск
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to: {}", filePath.toAbsolutePath());

            // 6. Вернуть URL для доступа к файлу (статический ресурс)
            String fileUrl = "/uploads/" + newFileName;
            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            log.error("Failed to save file", e);
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during upload", e);
            return ResponseEntity.status(500).body("Unexpected error: " + e.getMessage());
        }
    }
}