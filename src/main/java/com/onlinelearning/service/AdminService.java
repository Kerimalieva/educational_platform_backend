package com.onlinelearning.service;

import com.onlinelearning.dto.response.*;
import com.onlinelearning.entity.*;
import com.onlinelearning.exception.AccessDeniedException;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.*;
import com.onlinelearning.util.ConvertHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserAccountRepository userAccountRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final QuizRepository quizRepository;
    private final UserService userService;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final QuizGradeRepository quizGradeRepository;
    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public List<UserResponseForAdmin> getAllUsers() {
        log.info("Admin fetching all users");
        return userAccountRepository.findAll().stream()
                .map(user -> UserResponseForAdmin.builder()
                        .id(user.getUserAccountId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .userType(user.getUserType().getTypeName())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin deleting user id: {}", userId);
        UserAccount currentAdmin = userService.getCurrentUser();
        if (currentAdmin.getUserAccountId().equals(userId)) {
            throw new AccessDeniedException("Admin cannot delete themselves");
        }
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (user instanceof Instructor instructor) {
            courseRepository.deleteAll(instructor.getCourses());
            instructor.getCourses().clear();
        }

        else if (user instanceof Student student) {
            enrollmentRepository.deleteAll(student.getEnrollments());
            attendanceRepository.deleteAll(student.getAttendances());
            assignmentSubmissionRepository.deleteAll(student.getAssignmentSubmissions());
            quizGradeRepository.deleteAll(student.getQuizGrades());
            student.getEnrollments().clear();
            student.getAttendances().clear();
            student.getAssignmentSubmissions().clear();
            student.getQuizGrades().clear();
        }

        userAccountRepository.delete(user);
        log.info("User {} deleted by admin {}", userId, currentAdmin.getEmail());
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional
    public void deleteCourseByAdmin(Long courseId) {
        log.info("Admin deleting course id: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        courseRepository.delete(course);
    }

    @Transactional
    public void deleteLessonByAdmin(Long lessonId) {
        log.info("Admin deleting lesson id: {}", lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        lessonRepository.delete(lesson);
    }

    @Transactional
    public void deleteAssignmentByAdmin(Long assignmentId) {
        log.info("Admin deleting assignment id: {}", assignmentId);
        assignmentRepository.deleteById(assignmentId);
    }

    @Transactional
    public void deleteQuizByAdmin(Long quizId) {
        log.info("Admin deleting quiz id: {}", quizId);
        quizRepository.deleteById(quizId);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getStatistics() {
        long totalUsers = userAccountRepository.count();
        long totalCourses = courseRepository.count();
        long totalLessons = lessonRepository.count();
        long totalAssignments = assignmentRepository.count();
        long totalQuizzes = quizRepository.count();
        return Map.of(
                "totalUsers", totalUsers,
                "totalCourses", totalCourses,
                "totalLessons", totalLessons,
                "totalAssignments", totalAssignments,
                "totalQuizzes", totalQuizzes
        );
    }



    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return lessonRepository.findByCourseCourseIdOrderByLessonOrderAsc(courseId)
                .stream()
                .map(ConvertHelper::toLessonResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return assignmentRepository.findByCourseCourseId(courseId)
                .stream()
                .map(this::toAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return quizRepository.findByCourseCourseId(courseId)
                .stream()
                .map(this::toQuizResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getBankQuestionsByCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return questionRepository.findByCourseCourseId(courseId)
                .stream()
                .map(this::toQuestionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseContentResponse getCourseContent(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));
        return CourseContentResponse.builder()
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .lessons(getLessonsByCourse(courseId))
                .assignments(getAssignmentsByCourse(courseId))
                .quizzes(getQuizzesByCourse(courseId))
                .bankQuestions(getBankQuestionsByCourse(courseId))
                .build();
    }

    private AssignmentResponse toAssignmentResponse(Assignment assignment) {
        return AssignmentResponse.builder()
                .assignmentId(assignment.getAssignmentId())
                .assignmentTitle(assignment.getAssignmentTitle())
                .assignmentDescription(assignment.getAssignmentDescription())
                .dueDate(assignment.getDueDate())
                .courseId(assignment.getCourse().getCourseId())
                .courseName(assignment.getCourse().getCourseName())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    private QuizResponse toQuizResponse(Quiz quiz) {
        return QuizResponse.builder()
                .quizId(quiz.getQuizId())
                .type(quiz.getType())
                .isActive(quiz.getIsActive())
                .durationMinutes(quiz.getDurationMinutes())
                .courseId(quiz.getCourse().getCourseId())
                .courseName(quiz.getCourse().getCourseName())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private QuestionResponse toQuestionResponse(Question question) {
        return QuestionResponse.builder()
                .questionId(question.getQuestionId())
                .questionText(question.getQuestionText())
                .type(question.getType())
                .options(question.getOptions())
                .correctAnswer(question.getCorrectAnswer())
                .quizId(question.getQuiz() != null ? question.getQuiz().getQuizId() : null)
                .courseId(question.getCourse() != null ? question.getCourse().getCourseId() : null)
                .build();
    }
}