package com.onlinelearning;

import com.onlinelearning.dto.QuestionDTO;
import com.onlinelearning.dto.QuizDTO;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinelearning.service.QuizService;
import com.onlinelearning.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock private QuizRepository quizRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private QuizGradeRepository quizGradeRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserService userService;
    @Mock private NotificationRepository notificationRepository;

    @InjectMocks
    private QuizService quizService;

    private Instructor instructor;
    private Course course;
    private Quiz quiz;
    private Student student;

    @BeforeEach
    void setUp() {
        instructor = new Instructor();
        instructor.setUserAccountId(100L);

        course = new Course();
        course.setCourseId(1L);
        course.setInstructor(instructor);

        quiz = new Quiz();
        quiz.setQuizId(10L);
        quiz.setCourse(course);
        quiz.setType(1);
        quiz.setIsActive(true);

        student = new Student();
        student.setUserAccountId(200L);
    }

    @Test
    void createQuiz_Success() {
        QuizDTO dto = new QuizDTO();
        dto.setCourseId(1L);
        dto.setType(1);
        dto.setDurationMinutes(30);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(quizRepository.save(any(Quiz.class))).thenReturn(quiz);

        Quiz result = quizService.createQuiz(dto);
        assertNotNull(result);
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void addQuestionsBank_Success() {
        QuestionDTO qDto = new QuestionDTO();
        qDto.setQuestionText("Q1");
        qDto.setType(1);
        qDto.setOptions(List.of("A", "B"));
        qDto.setCorrectAnswer("A");

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(questionRepository.save(any(Question.class))).thenReturn(new Question());

        List<Question> questions = quizService.addQuestionsBank(List.of(qDto), 1L);
        assertEquals(1, questions.size());
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void gradeQuiz_Success() throws Exception {
        Question q = new Question();
        q.setCorrectAnswer("A");
        when(quizRepository.findById(10L)).thenReturn(Optional.of(quiz));
        when(studentRepository.findById(200L)).thenReturn(Optional.of(student));
        when(quizGradeRepository.findByStudentUserAccountIdAndQuizQuizId(200L, 10L))
                .thenReturn(Optional.empty());
        when(questionRepository.findByQuizQuizId(10L)).thenReturn(List.of(q));
        when(quizGradeRepository.save(any(QuizGrade.class))).thenAnswer(inv -> inv.getArgument(0));

        QuizGrade grade = quizService.gradeQuiz(10L, List.of("A"), 200L);
        assertEquals(100.0, grade.getGrade());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void gradeQuiz_AlreadySubmitted_ThrowsException() {
        when(quizRepository.findById(10L)).thenReturn(Optional.of(quiz));
        when(studentRepository.findById(200L)).thenReturn(Optional.of(student));
        when(quizGradeRepository.findByStudentUserAccountIdAndQuizQuizId(200L, 10L))
                .thenReturn(Optional.of(new QuizGrade()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> quizService.gradeQuiz(10L, List.of("A"), 200L));
        assertEquals("Quiz already submitted", ex.getMessage());
    }

    @Test
    void getQuizGrades_AsInstructor_Success() {
        when(quizRepository.findById(10L)).thenReturn(Optional.of(quiz));
        when(userService.getCurrentUser()).thenReturn(instructor);
        when(quizGradeRepository.findByQuizQuizId(10L)).thenReturn(List.of());

        List<QuizGrade> grades = quizService.getQuizGrades(10L);
        assertNotNull(grades);
    }

    @Test
    void getQuizGrades_NotInstructor_ThrowsException() {
        when(quizRepository.findById(10L)).thenReturn(Optional.of(quiz));
        when(userService.getCurrentUser()).thenReturn(student);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> quizService.getQuizGrades(10L));
        assertEquals("Only course instructor can view grades", ex.getMessage());
    }
}