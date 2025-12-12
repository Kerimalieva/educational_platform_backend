package com.onlinelearning.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "youtube_url")
    private String youtubeUrl;

    // геттер и сеттер
    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }
    @Column(name = "lesson_name", nullable = false)
    private String lessonName;

    @Column(name = "lesson_description", columnDefinition = "TEXT")
    private String lessonDescription;

    @Column(name = "lesson_order")
    private Integer lessonOrder;

    @Column(name = "otp", length = 6)
    private String OTP;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Attendance> attendances = new HashSet<>();

    // Constructors
    public Lesson() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.OTP = generateOTP();
    }

    public Lesson(String lessonName, String lessonDescription, Integer lessonOrder, String content, Course course) {
        this();
        this.lessonName = lessonName;
        this.lessonDescription = lessonDescription;
        this.lessonOrder = lessonOrder;
        this.content = content;
        this.course = course;
    }

    // Helper method to generate OTP
    private String generateOTP() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }

    // Regenerate OTP
    public void regenerateOTP() {
        this.OTP = generateOTP();
    }

    // Getters and Setters
    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public String getLessonDescription() {
        return lessonDescription;
    }

    public void setLessonDescription(String lessonDescription) {
        this.lessonDescription = lessonDescription;
    }

    public Integer getLessonOrder() {
        return lessonOrder;
    }

    public void setLessonOrder(Integer lessonOrder) {
        this.lessonOrder = lessonOrder;
    }

    public String getOTP() {
        return OTP;
    }

    public void setOTP(String OTP) {
        this.OTP = OTP;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Set<Attendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(Set<Attendance> attendances) {
        this.attendances = attendances;
    }

    public void addAttendance(Attendance attendance) {
        attendances.add(attendance);
        attendance.setLesson(this);
    }

    public void removeAttendance(Attendance attendance) {
        attendances.remove(attendance);
        attendance.setLesson(null);
    }
}