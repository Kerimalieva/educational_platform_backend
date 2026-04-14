package com.onlinelearning.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)(?!.* ).{8,}$",
            message = "Password must contain at least 8 characters, one digit, one lowercase, one uppercase, one special character, and no spaces")
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(STUDENT|INSTRUCTOR)$", message = "Role must be STUDENT or INSTRUCTOR")
    private String role;

    private String firstName;
    private String lastName;
}