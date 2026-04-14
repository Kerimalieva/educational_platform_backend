package com.onlinelearning;

import com.onlinelearning.dto.request.UserUpdateRequest;
import com.onlinelearning.dto.response.UserProfileResponse;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.entity.UserType;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.UserAccountRepository;
import com.onlinelearning.service.UserService;
import com.onlinelearning.util.ConvertHelper;
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
        assertNotNull(current);
        assertEquals("user@example.com", current.getEmail());
        assertEquals("John", current.getFirstName());
        assertEquals("Doe", current.getLastName());
    }

    @Test
    void getCurrentUser_UserNotFound_ThrowsResourceNotFoundException() {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("unknown@example.com");
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userAccountRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.getCurrentUser());
        assertEquals("User not found with id: 0", ex.getMessage());
        assertEquals("NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void updateUserProfile_Success() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(user);

        UserProfileResponse response = userService.updateUserProfile(1L, request);

        assertNotNull(response);
        assertEquals(1L, response.getUserAccountId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("Jane", response.getFirstName());
        assertEquals("Smith", response.getLastName());
        assertEquals("STUDENT", response.getUserType());

        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    void updateUserProfile_PartialUpdate_Success() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("Jane")
                .build();

        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(user);

        UserProfileResponse response = userService.updateUserProfile(1L, request);

        assertEquals("Jane", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void updateUserProfile_UserNotFound_ThrowsResourceNotFoundException() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("Jane")
                .build();

        when(userAccountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserProfile(99L, request));
        assertEquals("User not found with id: 99", ex.getMessage());
        assertEquals("NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void updateUserProfile_NullRequest_NoChanges() {
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(user);

        UserProfileResponse response = userService.updateUserProfile(1L, null);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }

    @Test
    void convertToProfileResponse_Success() {
        UserProfileResponse response = ConvertHelper.toUserProfileResponse(user);
        assertNotNull(response);
        assertEquals(1L, response.getUserAccountId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("STUDENT", response.getUserType());
    }
}