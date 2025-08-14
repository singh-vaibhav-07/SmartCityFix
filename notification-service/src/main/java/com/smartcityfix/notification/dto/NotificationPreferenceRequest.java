package com.smartcityfix.notification.dto;

import com.smartcityfix.notification.model.NotificationChannel;
import com.smartcityfix.notification.model.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private Set<NotificationChannel> enabledChannels;

    private Set<NotificationType> enabledTypes;

    private Boolean emailEnabled;

    private Boolean smsEnabled;

    private Boolean inAppEnabled;
}