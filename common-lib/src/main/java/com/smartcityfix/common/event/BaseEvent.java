package com.smartcityfix.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private UUID eventId;
    private LocalDateTime timestamp;
    private String eventType;

    public BaseEvent(String eventType) {
        this.eventId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
    }
}