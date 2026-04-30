package com.onlinelearning.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CourseContentResponse {
    private Long courseId;
    private String courseName;
    private List<LessonResponse> lessons;
    private List<AssignmentResponse> assignments;
    private List<QuizResponse> quizzes;
    private List<QuestionResponse> bankQuestions;
}