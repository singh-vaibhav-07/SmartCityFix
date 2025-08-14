package com.smartcityfix.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackCreatedEvent {

    private UUID feedbackId;
    private UUID complaintId;
    private UUID userId;
    private UUID departmentId;
    private Integer rating;
}