package com.smartcityfix.feedback.messaging;

import com.smartcityfix.common.event.ComplaintResolvedEvent;
import com.smartcityfix.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final FeedbackService feedbackService;

    @RabbitListener(queues = "${app.rabbitmq.queue.complaint-resolved}")
    public void handleComplaintResolvedEvent(ComplaintResolvedEvent event) {
        log.info("Received ComplaintResolvedEvent for complaint: {}", event.getComplaintId());

        try {
            // When a complaint is resolved, we don't create feedback automatically
            // but we could send a notification to the user asking for feedback
            log.info("Complaint {} has been resolved. User can now provide feedback.", event.getComplaintId());
        } catch (Exception e) {
            log.error("Error processing ComplaintResolvedEvent", e);
        }
    }
}