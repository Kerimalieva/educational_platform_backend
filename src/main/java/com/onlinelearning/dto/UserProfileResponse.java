package com.onlinelearning.dto;

public class UserProfileResponse {

    private Long userAccountId;
    private String email;
    private String firstName;
    private String lastName;
    private String userType;

    // Constructors
    public UserProfileResponse() {}

    public UserProfileResponse(Long userAccountId, String email, String firstName,
                               String lastName, String userType) {
        this.userAccountId = userAccountId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
    }

    // Getters and Setters
    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}