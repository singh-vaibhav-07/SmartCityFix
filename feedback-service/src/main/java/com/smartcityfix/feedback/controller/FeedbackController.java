package com.smartcityfix.feedback.controller;

import com.smartcityfix.common.dto.ApiResponse;
import com.smartcityfix.feedback.dto.*;
import com.smartcityfix.feedback.model.FeedbackStatus;
import com.smartcityfix.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feedback Management", description = "APIs for feedback management")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Create feedback", description = "Creates a new feedback for a resolved complaint")
    public ResponseEntity<ApiResponse<FeedbackResponse>> createFeedback(@Valid @RequestBody FeedbackRequest request) {
        log.info("Received request to create feedback for complaint: {}", request.getComplaintId());
        FeedbackResponse response = feedbackService.createFeedback(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feedback by ID", description = "Returns feedback details for the given ID")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedbackById(@PathVariable UUID id) {
        log.info("Fetching feedback with id: {}", id);
        FeedbackResponse response = feedbackService.getFeedbackById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/complaint/{complaintId}/user/{userId}")
    @Operation(summary = "Get feedback by complaint and user", description = "Returns feedback for the specified complaint and user")
    public ResponseEntity<ApiResponse<FeedbackResponse>> getFeedbackByComplaintIdAndUserId(
            @PathVariable UUID complaintId,
            @PathVariable UUID userId) {
        log.info("Fetching feedback for complaint: {} and user: {}", complaintId, userId);
        FeedbackResponse response = feedbackService.getFeedbackByComplaintIdAndUserId(complaintId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user feedbacks", description = "Returns feedbacks submitted by the specified user")
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getUserFeedbacks(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching feedbacks for user: {}, page: {}, size: {}", userId, page, size);
        Page<FeedbackResponse> response = feedbackService.getUserFeedbacks(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get department feedbacks", description = "Returns feedbacks for the specified department")
    public ResponseEntity<ApiResponse<Page<FeedbackResponse>>> getDepartmentFeedbacks(
            @PathVariable UUID departmentId,
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching feedbacks for department: {}, status: {}, page: {}, size: {}", departmentId, status, page, size);
        Page<FeedbackResponse> response = feedbackService.getDepartmentFeedbacks(departmentId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/status")
    @Operation(summary = "Update feedback status", description = "Updates the status of a feedback (PENDING, APPROVED, REJECTED)")
    public ResponseEntity<ApiResponse<FeedbackResponse>> updateFeedbackStatus(@Valid @RequestBody FeedbackStatusUpdateRequest request) {
        log.info("Updating feedback status: {}", request);
        FeedbackResponse response = feedbackService.updateFeedbackStatus(request);
        return ResponseEntity.ok(ApiResponse.success("Feedback status updated successfully", response));
    }

    @PostMapping("/responses")
    @Operation(summary = "Add feedback response", description = "Adds a response to a feedback")
    public ResponseEntity<ApiResponse<FeedbackResponseDto>> addFeedbackResponse(@Valid @RequestBody FeedbackResponseRequest request) {
        log.info("Adding response to feedback: {}", request.getFeedbackId());
        FeedbackResponseDto response = feedbackService.addFeedbackResponse(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Response added successfully", response));
    }

    @GetMapping("/{feedbackId}/responses")
    @Operation(summary = "Get feedback responses", description = "Returns responses for the specified feedback")
    public ResponseEntity<ApiResponse<List<FeedbackResponseDto>>> getFeedbackResponses(@PathVariable UUID feedbackId) {
        log.info("Fetching responses for feedback: {}", feedbackId);
        List<FeedbackResponseDto> responses = feedbackService.getFeedbackResponses(feedbackId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/ratings/department/{departmentId}")
    @Operation(summary = "Get department rating", description = "Returns rating statistics for the specified department")
    public ResponseEntity<ApiResponse<DepartmentRatingDto>> getDepartmentRating(@PathVariable UUID departmentId) {
        log.info("Fetching rating for department: {}", departmentId);
        DepartmentRatingDto rating = feedbackService.getDepartmentRating(departmentId);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }

    @GetMapping("/ratings/departments")
    @Operation(summary = "Get all department ratings", description = "Returns rating statistics for all departments")
    public ResponseEntity<ApiResponse<List<DepartmentRatingDto>>> getAllDepartmentRatings() {
        log.info("Fetching all department ratings");
        List<DepartmentRatingDto> ratings = feedbackService.getAllDepartmentRatings();
        return ResponseEntity.ok(ApiResponse.success(ratings));
    }

    @PostMapping("/ratings/department/{departmentId}/recalculate")
    @Operation(summary = "Recalculate department rating", description = "Recalculates rating statistics for the specified department")
    public ResponseEntity<ApiResponse<Void>> recalculateDepartmentRating(@PathVariable UUID departmentId) {
        log.info("Recalculating rating for department: {}", departmentId);
        feedbackService.recalculateDepartmentRating(departmentId);
        return ResponseEntity.ok(ApiResponse.successMessage("Department rating recalculated successfully"));
    }
}