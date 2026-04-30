package com.onlinelearning.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizResponse {
    private Long quizId;
    private Integer type;
    private Boolean isActive;
    private Integer durationMinutes;
    private Long courseId;
    private String courseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}