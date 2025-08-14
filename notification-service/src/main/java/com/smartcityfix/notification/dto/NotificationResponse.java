package com.smartcityfix.notification.dto;

import com.smartcityfix.notification.model.NotificationChannel;
import com.smartcityfix.notification.model.NotificationType;
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
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private NotificationType type;
    private UUID referenceId;
    private NotificationChannel channel;
    private boolean read;
    private boolean sent;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}