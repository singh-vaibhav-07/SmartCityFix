package com.smartcityfix.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserRegisteredEvent extends BaseEvent {
    private UUID userId;
    private String email;
    private String name;

    public UserRegisteredEvent(UUID userId, String email, String name) {
        super("USER_REGISTERED");
        this.userId = userId;
        this.email = email;
        this.name = name;
    }
}