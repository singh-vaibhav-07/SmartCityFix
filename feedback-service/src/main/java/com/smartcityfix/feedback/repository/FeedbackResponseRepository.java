package com.smartcityfix.feedback.repository;

import com.smartcityfix.feedback.model.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, UUID> {

    List<FeedbackResponse> findByFeedbackId(UUID feedbackId);
}