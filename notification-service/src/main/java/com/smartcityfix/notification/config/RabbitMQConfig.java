package com.smartcityfix.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.user-registered}")
    private String userRegisteredQueue;

    @Value("${app.rabbitmq.queue.complaint-created}")
    private String complaintCreatedQueue;

    @Value("${app.rabbitmq.queue.complaint-assigned}")
    private String complaintAssignedQueue;

    @Value("${app.rabbitmq.queue.complaint-resolved}")
    private String complaintResolvedQueue;

    @Value("${app.rabbitmq.routing-key.user-registered}")
    private String userRegisteredRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-created}")
    private String complaintCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-assigned}")
    private String complaintAssignedRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-resolved}")
    private String complaintResolvedRoutingKey;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(userRegisteredQueue, true);
    }

    @Bean
    public Queue complaintCreatedQueue() {
        return new Queue(complaintCreatedQueue, true);
    }

    @Bean
    public Queue complaintAssignedQueue() {
        return new Queue(complaintAssignedQueue, true);
    }

    @Bean
    public Queue complaintResolvedQueue() {
        return new Queue(complaintResolvedQueue, true);
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(exchange())
                .with(userRegisteredRoutingKey);
    }

    @Bean
    public Binding complaintCreatedBinding() {
        return BindingBuilder.bind(complaintCreatedQueue())
                .to(exchange())
                .with(complaintCreatedRoutingKey);
    }

    @Bean
    public Binding complaintAssignedBinding() {
        return BindingBuilder.bind(complaintAssignedQueue())
                .to(exchange())
                .with(complaintAssignedRoutingKey);
    }

    @Bean
    public Binding complaintResolvedBinding() {
        return BindingBuilder.bind(complaintResolvedQueue())
                .to(exchange())
                .with(complaintResolvedRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}