package com.onlinelearning.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseForAdmin {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String userType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}