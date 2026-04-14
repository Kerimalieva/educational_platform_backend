package com.onlinelearning.service;

import com.onlinelearning.dto.request.LoginRequest;
import com.onlinelearning.dto.request.SignupRequest;
import com.onlinelearning.dto.response.AuthResponse;
import com.onlinelearning.entity.*;
import com.onlinelearning.exception.BusinessException;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.*;
import com.onlinelearning.security.JwtUtil;
import com.onlinelearning.util.ConvertHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final StudentRepository studentRepository;
    private final InstructorRepository instructorRepository;
    private final UserTypeRepository userTypeRepository;

    @Transactional
    public AuthResponse register(SignupRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists", "DUPLICATE_EMAIL");
        }

        String roleName = request.getRole().toUpperCase();
        UserType userType = userTypeRepository.findByTypeName(roleName)
                .orElseThrow(() -> new BusinessException("Invalid user type: " + roleName, "INVALID_ROLE"));

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        UserAccount userAccount;
        switch (roleName) {
            case "STUDENT":
                Student student = new Student();
                student.setEmail(request.getEmail());
                student.setPassword(encodedPassword);
                student.setUserType(userType);
                userAccount = studentRepository.save(student);
                break;
            case "INSTRUCTOR":
                Instructor instructor = new Instructor();
                instructor.setEmail(request.getEmail());
                instructor.setPassword(encodedPassword);
                instructor.setUserType(userType);
                userAccount = instructorRepository.save(instructor);
                break;
            default:
                throw new BusinessException("Unsupported role", "INVALID_ROLE");
        }

        log.debug("User saved with id: {}", userAccount.getUserAccountId());

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        String jwt = jwtUtil.generateToken((User) auth.getPrincipal());

        return ConvertHelper.toAuthResponse(userAccount, jwt);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserAccount userAccount = userAccountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));

        String jwt = jwtUtil.generateToken((User) auth.getPrincipal());
        return ConvertHelper.toAuthResponse(userAccount, jwt);
    }

    public void logout() {
        log.info("Logout request, clearing security context");
        SecurityContextHolder.clearContext();
    }
}