package com.onlinelearning.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuestionResponse {
    private Long questionId;
    private String questionText;
    private Integer type;
    private List<String> options;
    private String correctAnswer;
    private Long quizId;
    private Long courseId;
}