package com.smartcityfix.feedback.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {

    @NotNull(message = "Complaint ID is required")
    private UUID complaintId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private String comment;

    @Builder.Default
    private boolean anonymous = false;
}