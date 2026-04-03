package com.onlinelearning;

import com.onlinelearning.dto.AuthRequest;
import com.onlinelearning.dto.AuthResponse;
import com.onlinelearning.entity.*;
import com.onlinelearning.repository.*;
import com.onlinelearning.security.JwtUtil;
import com.onlinelearning.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserAccountRepository userAccountRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private InstructorRepository instructorRepository;
    @Mock private UserTypeRepository userTypeRepository;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;
    private UserType studentType;
    private UserType instructorType;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");
        authRequest.setRole("STUDENT");

        studentType = new UserType("STUDENT");
        studentType.setUserTypeId(1L);
        instructorType = new UserType("INSTRUCTOR");
    }

    @Test
    void register_Student_Success() {
        when(userAccountRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userTypeRepository.findByTypeName("STUDENT")).thenReturn(Optional.of(studentType));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPass");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setUserAccountId(1L);
            return s;
        });

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        User principal = new User("test@example.com", "encodedPass", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(authRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("STUDENT", response.getUserType());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsException() {
        when(userAccountRepository.existsByEmail("test@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(authRequest));
        assertEquals("Email already exists", ex.getMessage());
        verify(userTypeRepository, never()).findByTypeName(any());
    }

    @Test
    void register_InvalidRole_ThrowsException() {
        authRequest.setRole("UNKNOWN");
        when(userAccountRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userTypeRepository.findByTypeName("UNKNOWN")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(authRequest));
        assertEquals("Invalid user type", ex.getMessage());
    }

    @Test
    void login_Success() {
        UserAccount user = new Student();
        user.setEmail("test@example.com");
        user.setUserType(studentType);
        user.setUserAccountId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        User principal = new User("test@example.com", "pass", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");
        when(userAccountRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(authRequest);

        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("STUDENT", response.getUserType());
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userAccountRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(authRequest));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void logout_ClearsContext() {
        assertDoesNotThrow(() -> authService.logout());
    }
}