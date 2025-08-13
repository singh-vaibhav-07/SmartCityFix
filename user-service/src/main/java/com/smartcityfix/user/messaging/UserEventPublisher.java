package com.smartcityfix.user.messaging;

import com.smartcityfix.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;

    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            log.info("Publishing UserRegisteredEvent for user: {}", event.getUserId());
            rabbitTemplate.convertAndSend(exchange, userRegisteredRoutingKey, event);
            log.info("UserRegisteredEvent published successfully");
        } catch (Exception e) {
            log.error("Failed to publish UserRegisteredEvent", e);
            throw e;
        }
    }
}