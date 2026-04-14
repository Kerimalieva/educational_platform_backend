package com.onlinelearning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long courseId;
    private String courseName;
    private String description;
    private Integer duration;
    private Long instructorId;
    private String instructorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}