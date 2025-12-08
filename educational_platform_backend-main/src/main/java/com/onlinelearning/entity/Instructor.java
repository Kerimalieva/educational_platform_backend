package com.onlinelearning.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "instructors")
@PrimaryKeyJoinColumn(name = "user_account_id")
public class Instructor extends UserAccount {

    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Course> courses = new HashSet<>();

    // Constructors
    public Instructor() {
        super();
    }

    public Instructor(String email, String password, String firstName, String lastName, UserType userType) {
        super(email, password, firstName, lastName, userType);
    }

    // Getters and Setters
    public Set<Course> getCourses() {
        return courses;
    }

    public void setCourses(Set<Course> courses) {
        this.courses = courses;
    }

    public void addCourse(Course course) {
        courses.add(course);
        course.setInstructor(this);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.setInstructor(null);
    }
}