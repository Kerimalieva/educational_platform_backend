package com.onlinelearning.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LessonDTO {

    private Long lessonId;

    @JsonProperty("lessonName")  // Позволяет фронтенду использовать "title" → "lessonName"
    private String lessonName;

    private String lessonDescription;
    private Integer lessonOrder;
    private String content;
    private Long courseId;
    private String courseName;

    // Новое поле для YouTube видео
    private String youtubeUrl;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    // Геттер и сеттер для YouTube URL
    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    // Опционально: @JsonProperty для фронтенда, если хочешь использовать "videoUrl"
    // @JsonProperty("videoUrl")
    // public String getYoutubeUrl() { ... }
}