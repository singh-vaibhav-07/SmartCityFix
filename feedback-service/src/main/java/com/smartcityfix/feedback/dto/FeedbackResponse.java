package com.smartcityfix.feedback.dto;

import com.smartcityfix.feedback.model.FeedbackStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private UUID id;
    private UUID complaintId;
    private UUID userId;
    private UUID departmentId;
    private Integer rating;
    private String comment;
    private FeedbackStatus status;
    private boolean anonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FeedbackResponseDto> responses;
}