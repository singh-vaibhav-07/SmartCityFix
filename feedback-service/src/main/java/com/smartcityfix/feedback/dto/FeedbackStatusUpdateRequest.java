package com.smartcityfix.feedback.dto;

import com.smartcityfix.feedback.model.FeedbackStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatusUpdateRequest {

    @NotNull(message = "Feedback ID is required")
    private UUID feedbackId;

    @NotNull(message = "Status is required")
    private FeedbackStatus status;
}