package com.smartcityfix.feedback.config;

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

    @Value("${app.rabbitmq.queue.complaint-resolved}")
    private String complaintResolvedQueue;

    @Value("${app.rabbitmq.queue.feedback-created}")
    private String feedbackCreatedQueue;

    @Value("${app.rabbitmq.routing-key.complaint-resolved}")
    private String complaintResolvedRoutingKey;

    @Value("${app.rabbitmq.routing-key.feedback-created}")
    private String feedbackCreatedRoutingKey;

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue complaintResolvedQueue() {
        return new Queue(complaintResolvedQueue, true);
    }

    @Bean
    public Queue feedbackCreatedQueue() {
        return new Queue(feedbackCreatedQueue, true);
    }

    @Bean
    public Binding complaintResolvedBinding() {
        return BindingBuilder.bind(complaintResolvedQueue())
                .to(exchange())
                .with(complaintResolvedRoutingKey);
    }

    @Bean
    public Binding feedbackCreatedBinding() {
        return BindingBuilder.bind(feedbackCreatedQueue())
                .to(exchange())
                .with(feedbackCreatedRoutingKey);
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