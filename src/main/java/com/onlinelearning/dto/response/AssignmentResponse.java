package com.onlinelearning.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AssignmentResponse {
    private Long assignmentId;
    private String assignmentTitle;
    private String assignmentDescription;
    private LocalDateTime dueDate;
    private Long courseId;
    private String courseName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}