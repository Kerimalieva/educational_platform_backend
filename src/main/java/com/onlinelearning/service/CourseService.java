package com.onlinelearning.service;

import com.onlinelearning.dto.request.CourseRequest;
import com.onlinelearning.dto.response.CourseResponse;
import com.onlinelearning.entity.Course;
import com.onlinelearning.entity.Instructor;
import com.onlinelearning.entity.UserAccount;
import com.onlinelearning.exception.AccessDeniedException;
import com.onlinelearning.exception.ResourceNotFoundException;
import com.onlinelearning.repository.CourseRepository;
import com.onlinelearning.util.ConvertHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserService userService;

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        log.info("Creating course: {}", request.getCourseName());
        UserAccount currentUser = userService.getCurrentUser();
        if (!(currentUser instanceof Instructor)) {
            throw new AccessDeniedException("Only instructors can create courses");
        }
        Instructor instructor = (Instructor) currentUser;

        Course course = ConvertHelper.toCourse(request, instructor);
        Course saved = courseRepository.save(course);
        log.info("Course created with id: {}", saved.getCourseId());
        return ConvertHelper.toCourseResponse(saved);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        log.debug("Fetching course by id: {}", id);
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
        return ConvertHelper.toCourseResponse(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getAllCourses(Pageable pageable) {
        log.debug("Fetching all courses with pagination: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return courseRepository.findAll(pageable)
                .map(ConvertHelper::toCourseResponse);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getMyCourses(Pageable pageable) {
        UserAccount currentUser = userService.getCurrentUser();
        if (!(currentUser instanceof Instructor)) {
            throw new AccessDeniedException("Only instructors can view their own courses");
        }
        Long instructorId = currentUser.getUserAccountId();
        log.debug("Fetching courses for instructor id: {}", instructorId);
        return courseRepository.findByInstructorUserAccountId(instructorId, pageable)
                .map(ConvertHelper::toCourseResponse);
    }

    @Transactional
    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        log.info("Updating course id: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new AccessDeniedException("Only course instructor can update the course");
        }

        if (request.getCourseName() != null) {
            course.setCourseName(request.getCourseName());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }
        if (request.getDuration() != null) {
            course.setDuration(request.getDuration());
        }
        course.setUpdatedAt(java.time.LocalDateTime.now());

        Course updated = courseRepository.save(course);
        log.info("Course updated: {}", updated.getCourseId());
        return ConvertHelper.toCourseResponse(updated);
    }

    @Transactional
    public void deleteCourse(Long courseId) {
        log.info("Deleting course id: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        UserAccount currentUser = userService.getCurrentUser();
        if (!course.getInstructor().getUserAccountId().equals(currentUser.getUserAccountId())) {
            throw new AccessDeniedException("Only course instructor can delete the course");
        }

        courseRepository.delete(course);
        log.info("Course deleted: {}", courseId);
    }
}