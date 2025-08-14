package com.smartcityfix.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponseDto {

    private UUID id;
    private UUID feedbackId;
    private UUID responderId;
    private String response;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}