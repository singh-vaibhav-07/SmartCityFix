package com.smartcityfix.feedback.service;

import com.smartcityfix.feedback.dto.*;
import com.smartcityfix.feedback.model.FeedbackStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface FeedbackService {

    FeedbackResponse createFeedback(FeedbackRequest request);

    FeedbackResponse getFeedbackById(UUID id);

    FeedbackResponse getFeedbackByComplaintIdAndUserId(UUID complaintId, UUID userId);

    Page<FeedbackResponse> getUserFeedbacks(UUID userId, int page, int size);

    Page<FeedbackResponse> getDepartmentFeedbacks(UUID departmentId, FeedbackStatus status, int page, int size);

    FeedbackResponse updateFeedbackStatus(FeedbackStatusUpdateRequest request);

    FeedbackResponseDto addFeedbackResponse(FeedbackResponseRequest request);

    List<FeedbackResponseDto> getFeedbackResponses(UUID feedbackId);

    DepartmentRatingDto getDepartmentRating(UUID departmentId);

    List<DepartmentRatingDto> getAllDepartmentRatings();

    void recalculateDepartmentRating(UUID departmentId);
}