package com.onlinelearning.util;

import com.onlinelearning.dto.request.SignupRequest;
import com.onlinelearning.dto.response.AuthResponse;
import com.onlinelearning.dto.response.UserProfileResponse;
import com.onlinelearning.entity.*;
import com.onlinelearning.dto.request.CourseRequest;
import com.onlinelearning.dto.response.CourseResponse;
import com.onlinelearning.dto.request.LessonRequest;
import com.onlinelearning.dto.response.LessonResponse;
import com.onlinelearning.dto.response.AttendanceResponse;
import com.onlinelearning.dto.response.EnrollmentResponse;
import com.onlinelearning.dto.response.NotificationResponse;




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



    public static LessonResponse toLessonResponse(Lesson lesson) {
        if (lesson == null) return null;
        return LessonResponse.builder()
                .lessonId(lesson.getLessonId())
                .lessonName(lesson.getLessonName())
                .lessonDescription(lesson.getLessonDescription())
                .lessonOrder(lesson.getLessonOrder())
                .content(lesson.getContent())
                .otp(lesson.getOTP())
                .courseId(lesson.getCourse().getCourseId())
                .courseName(lesson.getCourse().getCourseName())
                .createdAt(lesson.getCreatedAt())
                .updatedAt(lesson.getUpdatedAt())
                .build();
    }

    public static Lesson toLesson(LessonRequest request, Course course) {
        if (request == null) return null;
        Lesson lesson = new Lesson();
        lesson.setLessonName(request.getLessonName());
        lesson.setLessonDescription(request.getLessonDescription());
        lesson.setLessonOrder(request.getLessonOrder());
        lesson.setContent(request.getContent());
        lesson.setCourse(course);
        return lesson;
    }

    public static AttendanceResponse toAttendanceResponse(Attendance attendance) {
        if (attendance == null) return null;
        return AttendanceResponse.builder()
                .attendanceId(attendance.getAttendanceId())
                .studentId(attendance.getStudent().getUserAccountId())
                .studentName(attendance.getStudent().getFirstName() + " " + attendance.getStudent().getLastName())
                .lessonId(attendance.getLesson().getLessonId())
                .lessonName(attendance.getLesson().getLessonName())
                .attendedAt(attendance.getAttendedAt())
                .build();
    }


    public static EnrollmentResponse toEnrollmentResponse(Enrollment enrollment) {
        if (enrollment == null) return null;
        return EnrollmentResponse.builder()
                .enrollmentId(enrollment.getEnrollmentId())
                .studentId(enrollment.getStudent().getUserAccountId())
                .studentName(enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName())
                .studentEmail(enrollment.getStudent().getEmail())
                .courseId(enrollment.getCourse().getCourseId())
                .courseName(enrollment.getCourse().getCourseName())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }

    public static Enrollment toEnrollment(Student student, Course course) {
        return new Enrollment(student, course);
    }

    public static NotificationResponse toNotificationResponse(Notification notification) {
        if (notification == null) return null;
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .notificationType(notification.getNotificationType())
                .referenceId(notification.getReferenceId())
                .userId(notification.getUser().getUserAccountId())
                .build();
    }

}