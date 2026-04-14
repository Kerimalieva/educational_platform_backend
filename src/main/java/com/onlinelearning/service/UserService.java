package com.onlinelearning.service;

import com.onlinelearning.dto.response.UserProfileResponse;
import com.onlinelearning.dto.request.UserUpdateRequest;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.UserAccountRepository;
import com.onlinelearning.util.ConvertHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository userAccountRepository;

    public UserAccount getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", 0L));
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest updateRequest) {
        log.info("Updating profile for user id: {}", userId);

        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (updateRequest != null) {
            if (updateRequest.getFirstName() != null) {
                userAccount.setFirstName(updateRequest.getFirstName());
            }
            if (updateRequest.getLastName() != null) {
                userAccount.setLastName(updateRequest.getLastName());
            }
        } else {
            log.debug("Update request is null, no changes applied");
        }

        userAccount.setUpdatedAt(LocalDateTime.now());
        UserAccount updated = userAccountRepository.save(userAccount);
        log.debug("Profile updated for user: {}", updated.getEmail());

        return ConvertHelper.toUserProfileResponse(updated);
    }
}