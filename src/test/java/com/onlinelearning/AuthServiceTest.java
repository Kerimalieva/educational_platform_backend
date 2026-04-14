package com.onlinelearning;

import com.onlinelearning.dto.request.LoginRequest;
import com.onlinelearning.dto.request.SignupRequest;
import com.onlinelearning.dto.response.AuthResponse;
import com.onlinelearning.entity.*;
import com.onlinelearning.exception.BusinessException;
import com.onlinelearning.exception.ResourceNotFoundException;
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

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UserType studentType;
    private UserType instructorType;

    @BeforeEach
    void setUp() {
        signupRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .role("STUDENT")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        studentType = new UserType("STUDENT");
        studentType.setUserTypeId(1L);
        instructorType = new UserType("INSTRUCTOR");
    }


    @Test
    void register_Student_Success() {
        when(userAccountRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userTypeRepository.findByTypeName("STUDENT")).thenReturn(Optional.of(studentType));
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPass");
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

        AuthResponse response = authService.register(signupRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("STUDENT", response.getUserType());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void register_Instructor_Success() {
        SignupRequest instructorRequest = SignupRequest.builder()
                .email("instructor@test.com")
                .password("Password123!")
                .role("INSTRUCTOR")
                .build();

        when(userAccountRepository.existsByEmail("instructor@test.com")).thenReturn(false);
        when(userTypeRepository.findByTypeName("INSTRUCTOR")).thenReturn(Optional.of(instructorType));
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPass");
        when(instructorRepository.save(any(Instructor.class))).thenAnswer(inv -> {
            Instructor i = inv.getArgument(0);
            i.setUserAccountId(2L);
            return i;
        });

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        User principal = new User("instructor@test.com", "encodedPass", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(instructorRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("instructor@test.com", response.getEmail());
        assertEquals("INSTRUCTOR", response.getUserType());
        verify(instructorRepository).save(any(Instructor.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsBusinessException() {
        when(userAccountRepository.existsByEmail("test@example.com")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(signupRequest));
        assertEquals("Email already exists", ex.getMessage());
        assertEquals("DUPLICATE_EMAIL", ex.getErrorCode());
        verify(userTypeRepository, never()).findByTypeName(any());
    }

    @Test
    void register_InvalidRole_ThrowsBusinessException() {
        SignupRequest invalidRoleRequest = SignupRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .role("UNKNOWN")
                .build();

        when(userAccountRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userTypeRepository.findByTypeName("UNKNOWN")).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(invalidRoleRequest));
        assertEquals("Invalid user type: UNKNOWN", ex.getMessage());
        assertEquals("INVALID_ROLE", ex.getErrorCode());
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

        AuthResponse response = authService.login(loginRequest);

        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("STUDENT", response.getUserType());
    }

    @Test
    void login_UserNotFound_ThrowsResourceNotFoundException() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userAccountRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> authService.login(loginRequest));
        assertEquals("User not found with id: 0", ex.getMessage());
        assertEquals("NOT_FOUND", ex.getErrorCode());
    }


    @Test
    void logout_ClearsContext() {
        assertDoesNotThrow(() -> authService.logout());
    }
}