package com.smartcityfix.notification.dto;

import com.smartcityfix.notification.model.NotificationChannel;
import com.smartcityfix.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private UUID userId;
    private Set<NotificationChannel> enabledChannels;
    private Set<NotificationType> enabledTypes;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean inAppEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}