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
public class AttendanceResponse {
    private Long attendanceId;
    private Long studentId;
    private String studentName;
    private Long lessonId;
    private String lessonName;
    private LocalDateTime attendedAt;
}