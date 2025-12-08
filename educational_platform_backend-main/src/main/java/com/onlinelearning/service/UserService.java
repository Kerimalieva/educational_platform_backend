package com.onlinelearning.service;

import com.onlinelearning.dto.UserProfileResponse;
import com.onlinelearning.dto.UserUpdateRequest;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    public UserAccount getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public UserAccount updateUserProfile(Long userId, UserUpdateRequest updateRequest) {
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateRequest.getFirstName() != null) {
            userAccount.setFirstName(updateRequest.getFirstName());
        }

        if (updateRequest.getLastName() != null) {
            userAccount.setLastName(updateRequest.getLastName());
        }

        userAccount.setUpdatedAt(java.time.LocalDateTime.now());

        return userAccountRepository.save(userAccount);
    }

    // Новый метод для преобразования в DTO
    public UserProfileResponse convertToProfileResponse(UserAccount userAccount) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUserAccountId(userAccount.getUserAccountId());
        response.setEmail(userAccount.getEmail());
        response.setFirstName(userAccount.getFirstName());
        response.setLastName(userAccount.getLastName());
        response.setUserType(userAccount.getUserType().getTypeName());
        return response;
    }
}