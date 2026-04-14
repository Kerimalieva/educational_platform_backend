package com.onlinelearning.service;

import com.onlinelearning.dto.request.EnrollmentRequest;
import com.onlinelearning.dto.response.EnrollmentResponse;
import com.onlinelearning.entity.*;
import com.onlinelearning.exception.AccessDeniedException;
import com.onlinelearning.exception.BusinessException;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.*;
import com.onlinelearning.util.ConvertHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final UserService userService;
    private final NotificationRepository notificationRepository;

    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        log.info("Enrolling student {} into course {}", request.getStudentId(), request.getCourseId());

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", request.getStudentId()));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.getCourseId()));

        if (enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(request.getStudentId(), request.getCourseId())) {
            throw new BusinessException("Already enrolled in this course", "DUPLICATE_ENROLLMENT");
        }

        Enrollment enrollment = ConvertHelper.toEnrollment(student, course);
        Enrollment saved = enrollmentRepository.save(enrollment);

        Notification notification = new Notification(
                "Student " + student.getFirstName() + " " + student.getLastName() +
                        " has enrolled in your course: " + course.getCourseName(),
                "enrollment",
                course.getInstructor(),
                saved.getEnrollmentId()
        );
        notificationRepository.save(notification);

        log.info("Student {} enrolled, enrollment id: {}", student.getUserAccountId(), saved.getEnrollmentId());
        return ConvertHelper.toEnrollmentResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        log.debug("Fetching enrollments for student {}", studentId);
        UserAccount current = userService.getCurrentUser();
        if (!current.getUserAccountId().equals(studentId) && !(current instanceof Instructor)) {
            throw new AccessDeniedException("You can only view your own enrollments");
        }
        return enrollmentRepository.findByStudentUserAccountId(studentId).stream()
                .map(ConvertHelper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByCourse(Long courseId) {
        log.debug("Fetching enrollments for course {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new AccessDeniedException("Only course instructor can view enrolled students");
        }

        return enrollmentRepository.findByCourseCourseId(courseId).stream()
                .map(ConvertHelper::toEnrollmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void unenrollStudent(Long studentId, Long courseId) {
        log.info("Unenrolling student {} from course {}", studentId, courseId);
        Enrollment enrollment = enrollmentRepository
                .findByStudentUserAccountIdAndCourseCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for student " + studentId + " and course " + courseId));

        UserAccount currentUser = userService.getCurrentUser();
        if (!enrollment.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())
                && !currentUser.getUserAccountId().equals(studentId)) {
            throw new AccessDeniedException("Only course instructor or the student himself can unenroll");
        }

        enrollmentRepository.delete(enrollment);
        log.info("Unenrolled successfully");
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByStudent(Long studentId) {
        return enrollmentRepository.findByStudentUserAccountId(studentId).stream()
                .map(Enrollment::getCourse)
                .collect(Collectors.toList());
    }
}