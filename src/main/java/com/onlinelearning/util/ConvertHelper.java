package com.onlinelearning.util;

import com.onlinelearning.dto.request.SignupRequest;
import com.onlinelearning.dto.response.AuthResponse;
import com.onlinelearning.dto.response.UserProfileResponse;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.entity.UserType;


public final class ConvertHelper {

    public static AuthResponse toAuthResponse(UserAccount user, String token) {
        if (user == null) return null;
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userId(user.getUserAccountId())
                .userType(user.getUserType() != null ? user.getUserType().getTypeName() : null)
                .build();
    }

    public static UserProfileResponse toUserProfileResponse(UserAccount user) {
        if (user == null) return null;
        return UserProfileResponse.builder()
                .userAccountId(user.getUserAccountId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userType(user.getUserType() != null ? user.getUserType().getTypeName() : null)
                .build();
    }

    public static UserAccount toUserAccount(SignupRequest request, UserType userType, String encodedPassword) {
        if (request == null || userType == null) return null;
        UserAccount account = new UserAccount();
        account.setEmail(request.getEmail());
        account.setPassword(encodedPassword);
        account.setUserType(userType);
        return account;
    }
}