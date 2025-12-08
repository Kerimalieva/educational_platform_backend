package com.onlinelearning.controller;

import com.onlinelearning.dto.QuestionDTO;
import com.onlinelearning.dto.QuizDTO;
import com.onlinelearning.entity.Question;
import com.onlinelearning.entity.Quiz;
import com.onlinelearning.entity.QuizGrade;
import com.onlinelearning.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PostMapping("/add_quiz")
    public ResponseEntity<Quiz> addQuiz(@Valid @RequestBody QuizDTO quizDTO) {
        Quiz quiz = quizService.createQuiz(quizDTO);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/add_questions_bank")
    public ResponseEntity<List<Question>> addQuestionsBank(
            @RequestBody Map<String, Object> request) {

        Long courseId = Long.valueOf(request.get("course_id").toString());
        List<Map<String, Object>> questionList = (List<Map<String, Object>>) request.get("questionList");

        List<QuestionDTO> questionDTOs = questionList.stream()
                .map(q -> {
                    QuestionDTO dto = new QuestionDTO();
                    dto.setQuestionText((String) q.get("question_text"));
                    dto.setType((Integer) q.get("type"));
                    dto.setOptions((List<String>) q.get("options"));
                    dto.setCorrectAnswer((String) q.get("correct_answer"));
                    dto.setCourseId(courseId);
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());

        List<Question> questions = quizService.addQuestionsBank(questionDTOs, courseId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/add_questions")
    public ResponseEntity<Question> addQuestion(@Valid @RequestBody QuestionDTO questionDTO) {
        Question question = quizService.addQuestion(questionDTO);
        return ResponseEntity.ok(question);
    }

    @GetMapping("/quiz_id/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        Quiz quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    @GetMapping("/active_quiz/{course_id}")
    public ResponseEntity<List<QuizDTO>> getActiveQuizzes(@PathVariable("course_id") Long courseId) {
        List<QuizDTO> quizzes = quizService.getActiveQuizzes(courseId);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/get_question_bank/{id}")
    public ResponseEntity<List<Question>> getQuestionBank(@PathVariable("id") Long courseId) {
        List<Question> questions = quizService.getQuestionBank(courseId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/grade_quiz")
    public ResponseEntity<QuizGrade> gradeQuiz(@RequestBody Map<String, Object> request) {
        Long quizId = Long.valueOf(request.get("quiz_id").toString());
        List<String> answers = (List<String>) request.get("answers");
        Long studentId = Long.valueOf(request.get("student_id").toString());

        QuizGrade grade = quizService.gradeQuiz(quizId, answers, studentId);
        return ResponseEntity.ok(grade);
    }

    @GetMapping("/get_quiz_grade/{quiz_id}/student/{student_id}")
    public ResponseEntity<Double> getQuizGrade(
            @PathVariable("quiz_id") Long quizId,
            @PathVariable("student_id") Long studentId) {

        Double grade = quizService.getQuizGrade(quizId, studentId);
        return ResponseEntity.ok(grade);
    }

    @GetMapping("/get_quiz_questions/{id}")
    public ResponseEntity<List<Question>> getQuizQuestions(@PathVariable Long id) {
        List<Question> questions = quizService.getQuizQuestions(id);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/grades/{quizId}")
    public ResponseEntity<List<QuizGrade>> getQuizGrades(@PathVariable Long quizId) {
        List<QuizGrade> grades = quizService.getQuizGrades(quizId);
        return ResponseEntity.ok(grades);
    }
}