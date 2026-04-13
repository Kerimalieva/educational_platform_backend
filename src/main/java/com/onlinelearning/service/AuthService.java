package com.onlinelearning.service;

import com.onlinelearning.dto.*;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import com.onlinelearning.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    // ====================== REGISTER ======================
    @Transactional
    public AuthResponse register(SignupRequest signupRequest) {
        // Проверяем, существует ли пользователь
        if (userAccountRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Получаем userType по имени роли ("STUDENT" / "INSTRUCTOR")
        String roleName = signupRequest.getRole().toUpperCase();
        UserType userType = userTypeRepository.findByTypeName(roleName)
                .orElseThrow(() -> new RuntimeException("Invalid user type: " + roleName));

        // Шифруем пароль
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
        UserAccount userAccount;

        // Создание пользователя по типу
        switch (roleName) {
            case "STUDENT":
                Student student = new Student();
                student.setEmail(signupRequest.getEmail());
                student.setPassword(encodedPassword);
                student.setUserType(userType);
                userAccount = studentRepository.save(student);
                break;
            case "INSTRUCTOR":
                Instructor instructor = new Instructor();
                instructor.setEmail(signupRequest.getEmail());
                instructor.setPassword(encodedPassword);
                instructor.setUserType(userType);
                userAccount = instructorRepository.save(instructor);
                break;
            default:
                UserAccount newUser = new UserAccount();
                newUser.setEmail(signupRequest.getEmail());
                newUser.setPassword(encodedPassword);
                newUser.setUserType(userType);
                userAccount = userAccountRepository.save(newUser);
                break;
        }

        // Аутентифицируем пользователя, чтобы сразу выдать JWT
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signupRequest.getEmail(), signupRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генерируем JWT-токен
        String jwt = jwtUtil.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal()
        );

        // Формируем ответ
        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setEmail(userAccount.getEmail());
        response.setFirstName(userAccount.getFirstName());
        response.setLastName(userAccount.getLastName());
        response.setUserId(userAccount.getUserAccountId());
        response.setUserType(userType.getTypeName());

        return response;
    }

    // ====================== LOGIN (без роли) ======================
    public AuthResponse login(LoginRequest loginRequest) {
        // Проверяем email/пароль
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Генерация JWT
        String jwt = jwtUtil.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal()
        );

        // Получаем данные учетной записи
        UserAccount userAccount = userAccountRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Формируем AuthResponse
        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setEmail(userAccount.getEmail());
        response.setFirstName(userAccount.getFirstName());
        response.setLastName(userAccount.getLastName());
        response.setUserId(userAccount.getUserAccountId());
        response.setUserType(userAccount.getUserType().getTypeName());

        return response;
    }

    // ====================== LOGOUT ======================
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
