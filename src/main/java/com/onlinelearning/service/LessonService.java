package com.onlinelearning.service;

import com.onlinelearning.dto.request.*;
import com.onlinelearning.dto.response.*;
import com.onlinelearning.entity.*;
import com.onlinelearning.exception.AccessDeniedException;
import com.onlinelearning.exception.BusinessException;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.*;
import com.onlinelearning.util.ConvertHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserService userService;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public LessonResponse createLesson(LessonRequest request) {
        log.info("Creating lesson: {} for courseId: {}", request.getLessonName(), request.getCourseId());

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new AccessDeniedException("Only course instructor can create lessons");
        }

        Lesson lesson = ConvertHelper.toLesson(request, course);
        Lesson saved = lessonRepository.save(lesson);
        log.info("Lesson created with id: {}", saved.getLessonId());
        return ConvertHelper.toLessonResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<LessonResponse> getLessonsByCourse(Long courseId, Pageable pageable) {
        log.debug("Fetching lessons for courseId: {}, page: {}", courseId, pageable.getPageNumber());
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        UserAccount currentUser = userService.getCurrentUser();
        boolean isInstructor = course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId());
        boolean isEnrolledStudent = false;

        if (currentUser instanceof Student) {
            isEnrolledStudent = enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(
                    currentUser.getUserAccountId(), courseId);
        }

        if (!isInstructor && !isEnrolledStudent) {
            throw new AccessDeniedException("You are not enrolled in this course or not the instructor");
        }

        return lessonRepository.findByCourseCourseIdOrderByLessonOrderAsc(courseId, pageable)
                .map(ConvertHelper::toLessonResponse);
    }

    @Transactional(readOnly = true)
    public LessonResponse getLessonById(Long lessonId) {
        log.debug("Fetching lesson by id: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        return ConvertHelper.toLessonResponse(lesson);
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonRequest request) {
        log.info("Updating lesson id: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        UserAccount currentUser = userService.getCurrentUser();
        if (!lesson.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new AccessDeniedException("Only course instructor can update lessons");
        }

        if (request.getLessonName() != null) lesson.setLessonName(request.getLessonName());
        if (request.getLessonDescription() != null) lesson.setLessonDescription(request.getLessonDescription());
        if (request.getLessonOrder() != null) lesson.setLessonOrder(request.getLessonOrder());
        if (request.getContent() != null) lesson.setContent(request.getContent());
        lesson.setUpdatedAt(LocalDateTime.now());

        Lesson updated = lessonRepository.save(lesson);
        log.info("Lesson updated: {}", updated.getLessonId());
        return ConvertHelper.toLessonResponse(updated);
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        log.info("Deleting lesson id: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        UserAccount currentUser = userService.getCurrentUser();
        if (!lesson.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new AccessDeniedException("Only course instructor can delete lessons");
        }

        lessonRepository.delete(lesson);
        log.info("Lesson deleted: {}", lessonId);
    }

    @Transactional
    public AttendanceResponse studentEnterLesson(Long courseId, Long lessonId, String otp) {
        log.info("Student entering lesson: courseId={}, lessonId={}", courseId, lessonId);
        UserAccount currentUser = userService.getCurrentUser();

        if (!(currentUser instanceof Student)) {
            throw new AccessDeniedException("Only students can enter lessons");
        }
        Student student = (Student) currentUser;

        boolean isEnrolled = enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(
                student.getUserAccountId(), courseId);
        if (!isEnrolled) {
            throw new AccessDeniedException("Student is not enrolled in this course");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        if (!lesson.getCourse().getCourseId().equals(courseId)) {
            throw new BusinessException("Lesson does not belong to the specified course", "INVALID_MAPPING");
        }

        if (!lesson.getOTP().equals(otp)) {
            throw new BusinessException("Invalid OTP", "INVALID_OTP");
        }

        if (attendanceRepository.findByStudentUserAccountIdAndLessonLessonId(student.getUserAccountId(), lessonId).isPresent()) {
            throw new BusinessException("Attendance already marked for this lesson", "DUPLICATE_ATTENDANCE");
        }

        Attendance attendance = new Attendance(student, lesson);
        Attendance saved = attendanceRepository.save(attendance);
        log.info("Attendance marked: studentId={}, lessonId={}", student.getUserAccountId(), lessonId);
        return ConvertHelper.toAttendanceResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getLessonAttendances(Long lessonId) {
        log.debug("Fetching attendances for lessonId: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        UserAccount currentUser = userService.getCurrentUser();
        if (!lesson.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new AccessDeniedException("Only course instructor can view attendances");
        }

        return attendanceRepository.findByLessonLessonId(lessonId).stream()
                .map(ConvertHelper::toAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public String regenerateOtp(Long lessonId) {
        log.info("Regenerating OTP for lesson id: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));

        UserAccount currentUser = userService.getCurrentUser();
        if (!lesson.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new AccessDeniedException("Only course instructor can regenerate OTP");
        }

        lesson.regenerateOTP();
        Lesson updated = lessonRepository.save(lesson);
        log.info("OTP regenerated for lesson: {}", lessonId);
        return updated.getOTP();
    }
}