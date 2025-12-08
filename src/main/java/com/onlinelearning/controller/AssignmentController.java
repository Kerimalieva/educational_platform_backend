package com.onlinelearning.controller;

import com.onlinelearning.dto.AssignmentDTO;
import com.onlinelearning.entity.Assignment;
import com.onlinelearning.entity.AssignmentSubmission;
import com.onlinelearning.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assignment")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping("/add_assignment")
    public ResponseEntity<Assignment> addAssignment(@Valid @RequestBody AssignmentDTO assignmentDTO) {
        Assignment assignment = assignmentService.createAssignment(assignmentDTO);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/uploadAssignment")
    public ResponseEntity<AssignmentSubmission> uploadAssignment(
            @Valid @RequestBody AssignmentSubmission submission) {

        AssignmentSubmission savedSubmission = assignmentService.uploadAssignment(submission);
        return ResponseEntity.ok(savedSubmission);
    }

    @PutMapping("/gradeAssignment")
    public ResponseEntity<AssignmentSubmission> gradeAssignment(
            @RequestBody Map<String, Object> request) {

        Long studentId = Long.valueOf(request.get("studentId").toString());
        Long assignmentId = Long.valueOf(request.get("assignmentId").toString());
        Double grade = Double.valueOf(request.get("grade").toString());

        AssignmentSubmission graded = assignmentService.gradeAssignment(studentId, assignmentId, grade);
        return ResponseEntity.ok(graded);
    }

    @PutMapping("/saveAssignmentFeedback")
    public ResponseEntity<AssignmentSubmission> saveAssignmentFeedback(
            @RequestBody Map<String, Object> request) {

        Long studentId = Long.valueOf(request.get("studentId").toString());
        Long assignmentId = Long.valueOf(request.get("assignmentId").toString());
        String feedback = request.get("feedback").toString();

        AssignmentSubmission updated = assignmentService.saveAssignmentFeedback(studentId, assignmentId, feedback);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getFeedback")
    public ResponseEntity<String> getFeedback(@RequestBody Map<String, Object> request) {
        Long assignmentId = Long.valueOf(request.get("assignmentId").toString());
        String feedback = assignmentService.getFeedback(assignmentId);
        return ResponseEntity.ok(feedback);
    }

    @GetMapping("/submissions/{assignmentId}")
    public ResponseEntity<List<AssignmentSubmission>> getSubmissions(@PathVariable Long assignmentId) {
        List<AssignmentSubmission> submissions = assignmentService.getSubmissions(assignmentId);
        return ResponseEntity.ok(submissions);
    }
}