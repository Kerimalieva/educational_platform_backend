package com.onlinelearning.util;

import com.onlinelearning.dto.request.SignupRequest;
import com.onlinelearning.dto.response.AuthResponse;
import com.onlinelearning.dto.response.UserProfileResponse;
import com.onlinelearning.entity.Instructor;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.entity.UserType;
import com.onlinelearning.dto.request.CourseRequest;
import com.onlinelearning.dto.response.CourseResponse;
import com.onlinelearning.entity.Course;



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



    public static CourseResponse toCourseResponse(Course course) {
        if (course == null) return null;
        return CourseResponse.builder()
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .duration(course.getDuration())
                .instructorId(course.getInstructor().getUserAccountId())
                .instructorName(course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    public static Course toCourse(CourseRequest request, Instructor instructor) {
        if (request == null) return null;
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setDuration(request.getDuration());
        course.setInstructor(instructor);
        return course;
    }

}