package com.smartcityfix.complaint.config;

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

    @Value("${app.rabbitmq.queue.complaint-created}")
    private String complaintCreatedQueue;

    @Value("${app.rabbitmq.queue.complaint-assigned}")
    private String complaintAssignedQueue;

    @Value("${app.rabbitmq.queue.complaint-status-updated}")
    private String complaintStatusUpdatedQueue;

    @Value("${app.rabbitmq.queue.complaint-resolved}")
    private String complaintResolvedQueue;

    @Value("${app.rabbitmq.routing-key.complaint-created}")
    private String complaintCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-assigned}")
    private String complaintAssignedRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-status-updated}")
    private String complaintStatusUpdatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-resolved}")
    private String complaintResolvedRoutingKey;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
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
    public Queue complaintStatusUpdatedQueue() {
        return new Queue(complaintStatusUpdatedQueue, true);
    }

    @Bean
    public Queue complaintResolvedQueue() {
        return new Queue(complaintResolvedQueue, true);
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
    public Binding complaintStatusUpdatedBinding() {
        return BindingBuilder.bind(complaintStatusUpdatedQueue())
                .to(exchange())
                .with(complaintStatusUpdatedRoutingKey);
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