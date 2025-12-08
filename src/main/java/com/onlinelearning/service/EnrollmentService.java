package com.onlinelearning.service;

import com.onlinelearning.dto.EnrollmentDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Transactional
    public Enrollment enrollStudent(EnrollmentDTO enrollmentDTO) {
        Student student = studentRepository.findById(enrollmentDTO.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(enrollmentDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if already enrolled
        if (enrollmentRepository.existsByStudentUserAccountIdAndCourseCourseId(
                student.getUserAccountId(), course.getCourseId())) {
            throw new RuntimeException("Student is already enrolled in this course");
        }

        // Create enrollment
        Enrollment enrollment = new Enrollment(student, course);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // Create notification for instructor
        Notification notification = new Notification(
                "Student " + student.getFirstName() + " " + student.getLastName() +
                        " has enrolled in your course: " + course.getCourseName(),
                "enrollment",
                course.getInstructor(),
                savedEnrollment.getEnrollmentId()
        );
        notificationRepository.save(notification);

        return savedEnrollment;
    }

    public List<EnrollmentDTO> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentUserAccountId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> getEnrollmentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can view enrolled students");
        }

        return enrollmentRepository.findByCourseCourseId(courseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeEnrollment(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository
                .findByStudentUserAccountIdAndCourseCourseId(studentId, courseId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!enrollment.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can remove students from course");
        }

        enrollmentRepository.delete(enrollment);
    }

    private EnrollmentDTO convertToDTO(Enrollment enrollment) {
        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setEnrollmentId(enrollment.getEnrollmentId());
        dto.setStudentId(enrollment.getStudent().getUserAccountId());
        dto.setStudentName(enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName());
        dto.setStudentEmail(enrollment.getStudent().getEmail());
        dto.setCourseId(enrollment.getCourse().getCourseId());
        dto.setCourseName(enrollment.getCourse().getCourseName());
        dto.setEnrolledAt(enrollment.getEnrolledAt());
        return dto;
    }
}