package com.onlinelearning.service;

import com.onlinelearning.dto.QuestionDTO;
import com.onlinelearning.dto.QuizDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizGradeRepository quizGradeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationRepository notificationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Quiz createQuiz(QuizDTO quizDTO) {
        Course course = courseRepository.findById(quizDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can create quizzes");
        }

        Quiz quiz = new Quiz();
        quiz.setType(quizDTO.getType());
        quiz.setDurationMinutes(quizDTO.getDurationMinutes());
        quiz.setCourse(course);

        return quizRepository.save(quiz);
    }

    @Transactional
    public List<Question> addQuestionsBank(List<QuestionDTO> questionDTOs, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can add questions");
        }

        List<Question> questions = new ArrayList<>();
        for (QuestionDTO dto : questionDTOs) {
            Question question = new Question();
            question.setQuestionText(dto.getQuestionText());
            question.setType(dto.getType());
            question.setOptions(dto.getOptions());
            question.setCorrectAnswer(dto.getCorrectAnswer());
            question.setCourse(course);
            questions.add(questionRepository.save(question));
        }

        return questions;
    }

    @Transactional
    public Question addQuestion(QuestionDTO questionDTO) {
        Course course = courseRepository.findById(questionDTO.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can add questions");
        }

        Question question = new Question();
        question.setQuestionText(questionDTO.getQuestionText());
        question.setType(questionDTO.getType());
        question.setOptions(questionDTO.getOptions());
        question.setCorrectAnswer(questionDTO.getCorrectAnswer());

        if (questionDTO.getQuizId() != null) {
            Quiz quiz = quizRepository.findById(questionDTO.getQuizId())
                    .orElseThrow(() -> new RuntimeException("Quiz not found"));
            question.setQuiz(quiz);
        } else {
            question.setCourse(course);
        }

        return questionRepository.save(question);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    public List<QuizDTO> getActiveQuizzes(Long courseId) {
        return quizRepository.findByCourseCourseIdAndIsActiveTrue(courseId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Question> getQuestionBank(Long courseId) {
        return questionRepository.findByCourseCourseId(courseId);
    }

    @Transactional
    public QuizGrade gradeQuiz(Long quizId, List<String> answers, Long studentId) {
        Quiz quiz = getQuizById(quizId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if already graded
        quizGradeRepository.findByStudentUserAccountIdAndQuizQuizId(studentId, quizId)
                .ifPresent(grade -> {
                    throw new RuntimeException("Quiz already submitted");
                });

        // Calculate grade
        List<Question> questions = questionRepository.findByQuizQuizId(quizId);
        double score = 0;
        double total = questions.size();

        for (int i = 0; i < Math.min(answers.size(), questions.size()); i++) {
            if (questions.get(i).getCorrectAnswer().equals(answers.get(i))) {
                score++;
            }
        }

        double gradePercentage = (score / total) * 100;

        // Save grade
        QuizGrade quizGrade = new QuizGrade();
        quizGrade.setStudent(student);
        quizGrade.setQuiz(quiz);
        quizGrade.setGrade(gradePercentage);

        try {
            quizGrade.setAnswers(objectMapper.writeValueAsString(answers));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing answers");
        }

        QuizGrade savedGrade = quizGradeRepository.save(quizGrade);

        // Create notification
        Notification notification = new Notification(
                "Your quiz has been graded: " + String.format("%.1f", gradePercentage) + "%",
                "grade",
                student,
                savedGrade.getGradeId()
        );
        notificationRepository.save(notification);

        return savedGrade;
    }

    public Double getQuizGrade(Long quizId, Long studentId) {
        QuizGrade quizGrade = quizGradeRepository.findByStudentUserAccountIdAndQuizQuizId(studentId, quizId)
                .orElseThrow(() -> new RuntimeException("Grade not found"));
        return quizGrade.getGrade();
    }

    public List<Question> getQuizQuestions(Long quizId) {
        return questionRepository.findByQuizQuizId(quizId);
    }

    public List<QuizGrade> getQuizGrades(Long quizId) {
        Quiz quiz = getQuizById(quizId);

        // Check if current user is the course instructor
        UserAccount currentUser = userService.getCurrentUser();
        if (!quiz.getCourse().getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new RuntimeException("Only course instructor can view grades");
        }

        return quizGradeRepository.findByQuizQuizId(quizId);
    }

    private QuizDTO convertToDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setQuizId(quiz.getQuizId());
        dto.setType(quiz.getType());
        dto.setIsActive(quiz.getIsActive());
        dto.setDurationMinutes(quiz.getDurationMinutes());
        dto.setCourseId(quiz.getCourse().getCourseId());
        dto.setCourseName(quiz.getCourse().getCourseName());
        dto.setCreatedAt(quiz.getCreatedAt());
        return dto;
    }
}