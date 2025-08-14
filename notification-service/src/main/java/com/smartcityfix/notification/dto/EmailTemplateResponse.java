package com.smartcityfix.notification.dto;

import com.smartcityfix.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateResponse {

    private NotificationType type;
    private String subject;
    private String template;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}