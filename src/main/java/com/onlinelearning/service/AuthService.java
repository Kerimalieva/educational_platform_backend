package com.onlinelearning.service;

import com.onlinelearning.dto.AuthRequest;
import com.onlinelearning.dto.AuthResponse;
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
import java.util.Optional;

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

//    @Transactional
//    public AuthResponse register(AuthRequest authRequest) {
//        // Check if user already exists
//        if (userAccountRepository.existsByEmail(authRequest.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }
//
//        // Get user type
//        UserType userType = userTypeRepository.findById(authRequest.getUserTypeId())
//                .orElseThrow(() -> new RuntimeException("Invalid user type"));
//
//        // Create user based on type
//        UserAccount userAccount;
//        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());
//
//        if (userType.getTypeName().equals("STUDENT")) {
//            Student student = new Student();
//            student.setEmail(authRequest.getEmail());
//            student.setPassword(encodedPassword);
//            student.setUserType(userType);
//            userAccount = studentRepository.save(student);
//        } else if (userType.getTypeName().equals("INSTRUCTOR")) {
//            Instructor instructor = new Instructor();
//            instructor.setEmail(authRequest.getEmail());
//            instructor.setPassword(encodedPassword);
//            instructor.setUserType(userType);
//            userAccount = instructorRepository.save(instructor);
//        } else {
//            UserAccount newUser = new UserAccount();
//            newUser.setEmail(authRequest.getEmail());
//            newUser.setPassword(encodedPassword);
//            newUser.setUserType(userType);
//            userAccount = userAccountRepository.save(newUser);
//        }
//
//        // Authenticate the user
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        String jwt = jwtUtil.generateToken(
//                (org.springframework.security.core.userdetails.User) authentication.getPrincipal()
//        );
//
//        // Create response
//        AuthResponse response = new AuthResponse();
//        response.setToken(jwt);
//        response.setEmail(userAccount.getEmail());
//        response.setFirstName(userAccount.getFirstName());
//        response.setLastName(userAccount.getLastName());
//        response.setUserId(userAccount.getUserAccountId());
//        response.setUserType(userType.getTypeName());
//
//        return response;
//    }

    @Transactional
    public AuthResponse register(AuthRequest authRequest) {
        // Check if user already exists
        if (userAccountRepository.existsByEmail(authRequest.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Get user type by role name
        String roleName = authRequest.getRole().toUpperCase(); // "student" -> "STUDENT"
        UserType userType = userTypeRepository.findByTypeName(roleName)
                .orElseThrow(() -> new RuntimeException("Invalid user type"));

        // Create user based on type
        UserAccount userAccount;
        String encodedPassword = passwordEncoder.encode(authRequest.getPassword());

        switch (roleName) {
            case "STUDENT":
                Student student = new Student();
                student.setEmail(authRequest.getEmail());
                student.setPassword(encodedPassword);
                student.setUserType(userType);
                userAccount = studentRepository.save(student);
                break;
            case "INSTRUCTOR":
                Instructor instructor = new Instructor();
                instructor.setEmail(authRequest.getEmail());
                instructor.setPassword(encodedPassword);
                instructor.setUserType(userType);
                userAccount = instructorRepository.save(instructor);
                break;
            default:
                UserAccount newUser = new UserAccount();
                newUser.setEmail(authRequest.getEmail());
                newUser.setPassword(encodedPassword);
                newUser.setUserType(userType);
                userAccount = userAccountRepository.save(newUser);
                break;
        }

        // Authenticate the user and generate JWT
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal()
        );

        // Create response
        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setEmail(userAccount.getEmail());
        response.setFirstName(userAccount.getFirstName());
        response.setLastName(userAccount.getLastName());
        response.setUserId(userAccount.getUserAccountId());
        response.setUserType(userType.getTypeName());

        return response;
    }


    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal()
        );

        // Get user details
        UserAccount userAccount = userAccountRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create response
        AuthResponse response = new AuthResponse();
        response.setToken(jwt);
        response.setEmail(userAccount.getEmail());
        response.setFirstName(userAccount.getFirstName());
        response.setLastName(userAccount.getLastName());
        response.setUserId(userAccount.getUserAccountId());
        response.setUserType(userAccount.getUserType().getTypeName());

        return response;
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}