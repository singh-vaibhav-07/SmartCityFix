package com.smartcityfix.feedback.dto;

import jakarta.validation.constraints.NotBlank;
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
public class FeedbackResponseRequest {

    @NotNull(message = "Feedback ID is required")
    private UUID feedbackId;

    @NotNull(message = "Responder ID is required")
    private UUID responderId;

    @NotBlank(message = "Response is required")
    private String response;
}