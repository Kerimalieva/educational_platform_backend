package com.onlinelearning;

import com.onlinelearning.dto.UserProfileResponse;
import com.onlinelearning.dto.UserUpdateRequest;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.entity.UserType;
import com.onlinelearning.repository.UserAccountRepository;
import com.onlinelearning.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserAccountRepository userAccountRepository;

    @InjectMocks
    private UserService userService;

    private UserAccount user;
    private UserType userType;

    @BeforeEach
    void setUp() {
        userType = new UserType("STUDENT");
        user = new UserAccount();
        user.setUserAccountId(1L);
        user.setEmail("user@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setUserType(userType);
    }

    @Test
    void getCurrentUser_Success() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("user@example.com");
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userAccountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserAccount current = userService.getCurrentUser();
        assertEquals("user@example.com", current.getEmail());
    }

    @Test
    void updateUserProfile_Success() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(user);

        UserAccount updated = userService.updateUserProfile(1L, request);
        assertEquals("Jane", updated.getFirstName());
        assertEquals("Smith", updated.getLastName());
    }

    @Test
    void updateUserProfile_UserNotFound_ThrowsException() {
        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updateUserProfile(99L, new UserUpdateRequest()));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void convertToProfileResponse_Success() {
        UserProfileResponse response = userService.convertToProfileResponse(user);
        assertEquals(1L, response.getUserAccountId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("STUDENT", response.getUserType());
    }
}