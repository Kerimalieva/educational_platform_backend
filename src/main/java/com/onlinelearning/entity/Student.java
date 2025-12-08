package com.onlinelearning.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_account_id")
public class Student extends UserAccount {

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Enrollment> enrollments = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Attendance> attendances = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<AssignmentSubmission> assignmentSubmissions = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<QuizGrade> quizGrades = new HashSet<>();

    // Constructors
    public Student() {
        super();
    }

    public Student(String email, String password, String firstName, String lastName, UserType userType) {
        super(email, password, firstName, lastName, userType);
    }

    // Getters and Setters
    public Set<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(Set<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public void addEnrollment(Enrollment enrollment) {
        enrollments.add(enrollment);
        enrollment.setStudent(this);
    }

    public void removeEnrollment(Enrollment enrollment) {
        enrollments.remove(enrollment);
        enrollment.setStudent(null);
    }

    public Set<Attendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(Set<Attendance> attendances) {
        this.attendances = attendances;
    }

    public void addAttendance(Attendance attendance) {
        attendances.add(attendance);
        attendance.setStudent(this);
    }

    public void removeAttendance(Attendance attendance) {
        attendances.remove(attendance);
        attendance.setStudent(null);
    }

    public Set<AssignmentSubmission> getAssignmentSubmissions() {
        return assignmentSubmissions;
    }

    public void setAssignmentSubmissions(Set<AssignmentSubmission> assignmentSubmissions) {
        this.assignmentSubmissions = assignmentSubmissions;
    }

    public void addAssignmentSubmission(AssignmentSubmission assignmentSubmission) {
        assignmentSubmissions.add(assignmentSubmission);
        assignmentSubmission.setStudent(this);
    }

    public void removeAssignmentSubmission(AssignmentSubmission assignmentSubmission) {
        assignmentSubmissions.remove(assignmentSubmission);
        assignmentSubmission.setStudent(null);
    }

    public Set<QuizGrade> getQuizGrades() {
        return quizGrades;
    }

    public void setQuizGrades(Set<QuizGrade> quizGrades) {
        this.quizGrades = quizGrades;
    }

    public void addQuizGrade(QuizGrade quizGrade) {
        quizGrades.add(quizGrade);
        quizGrade.setStudent(this);
    }

    public void removeQuizGrade(QuizGrade quizGrade) {
        quizGrades.remove(quizGrade);
        quizGrade.setStudent(null);
    }
}