package com.onlinelearning.service;

import com.onlinelearning.dto.LessonDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Transactional
    public Lesson createLesson(LessonDTO lessonDTO) {
        Course course = courseRepository.findById(lessonDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can create lessons");
        }

        Lesson lesson = new Lesson();
        lesson.setLessonName(lessonDTO.getLessonName());
        lesson.setLessonDescription(lessonDTO.getLessonDescription());
        lesson.setLessonOrder(lessonDTO.getLessonOrder());
        lesson.setContent(lessonDTO.getContent());
        lesson.setYoutubeUrl(lessonDTO.getYoutubeUrl());
        lesson.setCourse(course);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        return lessonRepository.save(lesson);
    }

    public List<LessonDTO> getAllLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseCourseIdOrderByLessonOrderAsc(courseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Lesson getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    @Transactional
    public Lesson updateLesson(Long lessonId, LessonDTO lessonDTO) {
        Lesson lesson = getLessonById(lessonId);

        UserAccount currentUser = userService.getCurrentUser();
        if (!lesson.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can update lessons");
        }

        if (lessonDTO.getLessonName() != null && !lessonDTO.getLessonName().isBlank()) {
            lesson.setLessonName(lessonDTO.getLessonName());
        }
        if (lessonDTO.getLessonDescription() != null) {
            lesson.setLessonDescription(lessonDTO.getLessonDescription());
        }
        if (lessonDTO.getLessonOrder() != null) {
            lesson.setLessonOrder(lessonDTO.getLessonOrder());
        }
        if (lessonDTO.getContent() != null) {
            lesson.setContent(lessonDTO.getContent());
        }
        if (lessonDTO.getYoutubeUrl() != null) {
            lesson.setYoutubeUrl(lessonDTO.getYoutubeUrl().isBlank() ? null : lessonDTO.getYoutubeUrl().trim());
        }

        lesson.setUpdatedAt(LocalDateTime.now());

        return lessonRepository.save(lesson);
    }

    @Transactional
    public void deleteLesson(Long lessonId, Long courseId) {
        Lesson lesson = getLessonById(lessonId);

        if (!lesson.getCourse().getCourseId().equals(courseId)) {
            throw new RuntimeException("Lesson does not belong to the specified course");
        }

        UserAccount currentUser = userService.getCurrentUser();
        if (!lesson.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can delete lessons");
        }

        lessonRepository.delete(lesson);
    }

    // ==================== STUDENT ATTENDANCE ====================
    @Transactional
    public Attendance studentEnterLesson(Long courseId, Long lessonId, String otp) {
        UserAccount currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Student)) {
            throw new RuntimeException("Only students can enter lessons");
        }

        Student student = (Student) currentUser;

        boolean isEnrolled = enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(
                student.getUserAccountId(), courseId);

        if (!isEnrolled) {
            throw new RuntimeException("Student is not enrolled in this course");
        }

        Lesson lesson = getLessonById(lessonId);

        if (!lesson.getCourse().getCourseId().equals(courseId)) {
            throw new RuntimeException("Lesson does not belong to the specified course");
        }

        if (!lesson.getOTP().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        attendanceRepository.findByStudentUserAccountIdAndLessonLessonId(
                        student.getUserAccountId(), lessonId)
                .ifPresent(attendance -> {
                    throw new RuntimeException("Attendance already marked for this lesson");
                });

        Attendance attendance = new Attendance(student, lesson);
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getLessonAttendances(Long lessonId) {
        Lesson lesson = getLessonById(lessonId);

        UserAccount currentUser = userService.getCurrentUser();
        if (!lesson.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can view attendances");
        }

        return attendanceRepository.findByLessonLessonId(lessonId);
    }

    // ==================== DTO CONVERTER ====================
    private LessonDTO convertToDTO(Lesson lesson) {
        LessonDTO dto = new LessonDTO();
        dto.setLessonId(lesson.getLessonId());
        dto.setLessonName(lesson.getLessonName());
        dto.setLessonDescription(lesson.getLessonDescription());
        dto.setLessonOrder(lesson.getLessonOrder());
        dto.setContent(lesson.getContent());
        dto.setYoutubeUrl(lesson.getYoutubeUrl());
        dto.setCourseId(lesson.getCourse().getCourseId());
        dto.setCourseName(lesson.getCourse().getCourseName());
        return dto;
    }
}
