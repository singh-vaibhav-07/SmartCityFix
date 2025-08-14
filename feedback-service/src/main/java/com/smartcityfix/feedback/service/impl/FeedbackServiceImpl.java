package com.smartcityfix.feedback.service.impl;

import com.smartcityfix.common.exception.ResourceNotFoundException;
import com.smartcityfix.feedback.dto.*;
import com.smartcityfix.feedback.messaging.EventPublisher;
import com.smartcityfix.feedback.model.DepartmentRating;
import com.smartcityfix.feedback.model.Feedback;
import com.smartcityfix.feedback.model.FeedbackResponse;
import com.smartcityfix.feedback.model.FeedbackStatus;
import com.smartcityfix.feedback.repository.DepartmentRatingRepository;
import com.smartcityfix.feedback.repository.FeedbackRepository;
import com.smartcityfix.feedback.repository.FeedbackResponseRepository;
import com.smartcityfix.feedback.service.FeedbackService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackResponseRepository feedbackResponseRepository;
    private final DepartmentRatingRepository departmentRatingRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    @CircuitBreaker(name = "feedbackService", fallbackMethod = "createFeedbackFallback")
    @Retry(name = "feedbackService")
    public com.smartcityfix.feedback.dto.FeedbackResponse createFeedback(FeedbackRequest request) {
        log.info("Creating feedback for complaint: {}", request.getComplaintId());

        try {
            // Check if feedback already exists for this complaint and user
            feedbackRepository.findByComplaintIdAndUserId(request.getComplaintId(), request.getUserId())
                    .ifPresent(existingFeedback -> {
                        throw new IllegalArgumentException("Feedback already exists for this complaint and user");
                    });

            Feedback feedback = Feedback.builder()
                    .complaintId(request.getComplaintId())
                    .userId(request.getUserId())
                    .departmentId(request.getDepartmentId())
                    .rating(request.getRating())
                    .comment(request.getComment())
                    .status(FeedbackStatus.PENDING)
                    .anonymous(request.isAnonymous())
                    .build();

            Feedback savedFeedback = feedbackRepository.save(feedback);
            log.info("Feedback created with id: {}", savedFeedback.getId());

            // Publish event
            eventPublisher.publishFeedbackCreatedEvent(
                    savedFeedback.getId(),
                    savedFeedback.getComplaintId(),
                    savedFeedback.getUserId(),
                    savedFeedback.getDepartmentId(),
                    savedFeedback.getRating()
            );

            return mapToFeedbackResponse(savedFeedback);
        } catch (Exception e) {
            log.error("Error creating feedback", e);
            throw e;
        }
    }

    public com.smartcityfix.feedback.dto.FeedbackResponse createFeedbackFallback(FeedbackRequest request, Exception e) {
        log.error("Fallback: Error creating feedback", e);
        throw new RuntimeException("Unable to create feedback at this time. Please try again later.");
    }

    @Override
    @Transactional(readOnly = true)
    public com.smartcityfix.feedback.dto.FeedbackResponse getFeedbackById(UUID id) {
        log.info("Fetching feedback with id: {}", id);

        try {
            Feedback feedback = feedbackRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", id));

            return mapToFeedbackResponse(feedback);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching feedback with id: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public com.smartcityfix.feedback.dto.FeedbackResponse getFeedbackByComplaintIdAndUserId(UUID complaintId, UUID userId) {
        log.info("Fetching feedback for complaint: {} and user: {}", complaintId, userId);

        try {
            Feedback feedback = feedbackRepository.findByComplaintIdAndUserId(complaintId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Feedback", "complaintId and userId", complaintId + ", " + userId));

            return mapToFeedbackResponse(feedback);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching feedback for complaint: {} and user: {}", complaintId, userId, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<com.smartcityfix.feedback.dto.FeedbackResponse> getUserFeedbacks(UUID userId, int page, int size) {
        log.info("Fetching feedbacks for user: {}, page: {}, size: {}", userId, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Feedback> feedbacks = feedbackRepository.findByUserId(userId, pageable);

            return feedbacks.map(this::mapToFeedbackResponse);
        } catch (Exception e) {
            log.error("Error fetching feedbacks for user: {}", userId, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<com.smartcityfix.feedback.dto.FeedbackResponse> getDepartmentFeedbacks(UUID departmentId, FeedbackStatus status, int page, int size) {
        log.info("Fetching feedbacks for department: {}, status: {}, page: {}, size: {}", departmentId, status, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Feedback> feedbacks;

            if (status != null) {
                feedbacks = feedbackRepository.findByDepartmentIdAndStatus(departmentId, status, pageable);
            } else {
                feedbacks = feedbackRepository.findByDepartmentId(departmentId, pageable);
            }

            return feedbacks.map(this::mapToFeedbackResponse);
        } catch (Exception e) {
            log.error("Error fetching feedbacks for department: {}", departmentId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public com.smartcityfix.feedback.dto.FeedbackResponse updateFeedbackStatus(FeedbackStatusUpdateRequest request) {
        log.info("Updating feedback status: {}", request);

        try {
            Feedback feedback = feedbackRepository.findById(request.getFeedbackId())
                    .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", request.getFeedbackId()));

            feedback.setStatus(request.getStatus());
            Feedback updatedFeedback = feedbackRepository.save(feedback);
            log.info("Updated feedback status to: {}", request.getStatus());

            // If feedback is approved or rejected, recalculate department rating
            if (request.getStatus() == FeedbackStatus.APPROVED || request.getStatus() == FeedbackStatus.REJECTED) {
                recalculateDepartmentRating(feedback.getDepartmentId());
            }

            return mapToFeedbackResponse(updatedFeedback);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating feedback status", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public FeedbackResponseDto addFeedbackResponse(FeedbackResponseRequest request) {
        log.info("Adding response to feedback: {}", request.getFeedbackId());

        try {
            // Check if feedback exists
            feedbackRepository.findById(request.getFeedbackId())
                    .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", request.getFeedbackId()));

            FeedbackResponse response = FeedbackResponse.builder()
                    .feedbackId(request.getFeedbackId())
                    .responderId(request.getResponderId())
                    .response(request.getResponse())
                    .build();

            FeedbackResponse savedResponse = feedbackResponseRepository.save(response);
            log.info("Added response to feedback: {}", request.getFeedbackId());

            return mapToFeedbackResponseDto(savedResponse);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error adding response to feedback: {}", request.getFeedbackId(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackResponseDto> getFeedbackResponses(UUID feedbackId) {
        log.info("Fetching responses for feedback: {}", feedbackId);

        try {
            // Check if feedback exists
            feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new ResourceNotFoundException("Feedback", "id", feedbackId));

            List<FeedbackResponse> responses = feedbackResponseRepository.findByFeedbackId(feedbackId);

            return responses.stream()
                    .map(this::mapToFeedbackResponseDto)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching responses for feedback: {}", feedbackId, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentRatingDto getDepartmentRating(UUID departmentId) {
        log.info("Fetching rating for department: {}", departmentId);

        try {
            DepartmentRating rating = departmentRatingRepository.findById(departmentId)
                    .orElseGet(() -> {
                        // If no rating exists, create a default one
                        DepartmentRating newRating = DepartmentRating.builder()
                                .departmentId(departmentId)
                                .build();
                        return departmentRatingRepository.save(newRating);
                    });

            return mapToDepartmentRatingDto(rating);
        } catch (Exception e) {
            log.error("Error fetching rating for department: {}", departmentId, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentRatingDto> getAllDepartmentRatings() {
        log.info("Fetching all department ratings");

        try {
            List<DepartmentRating> ratings = departmentRatingRepository.findAllOrderByAverageRatingDesc();

            return ratings.stream()
                    .map(this::mapToDepartmentRatingDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all department ratings", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void recalculateDepartmentRating(UUID departmentId) {
        log.info("Recalculating rating for department: {}", departmentId);

        try {
            // Get the average rating
            Double averageRating = feedbackRepository.findAverageRatingByDepartmentId(departmentId);
            if (averageRating == null) {
                averageRating = 0.0;
            }

            // Get the total number of approved ratings
            Integer totalRatings = feedbackRepository.countByDepartmentId(departmentId);

            // Get the count for each rating
            Integer rating1Count = feedbackRepository.countByDepartmentIdAndRating(departmentId, 1);
            Integer rating2Count = feedbackRepository.countByDepartmentIdAndRating(departmentId, 2);
            Integer rating3Count = feedbackRepository.countByDepartmentIdAndRating(departmentId, 3);
            Integer rating4Count = feedbackRepository.countByDepartmentIdAndRating(departmentId, 4);
            Integer rating5Count = feedbackRepository.countByDepartmentIdAndRating(departmentId, 5);

            // Update or create the department rating
            DepartmentRating rating = departmentRatingRepository.findById(departmentId)
                    .orElseGet(() -> DepartmentRating.builder().departmentId(departmentId).build());

            rating.setAverageRating(averageRating);
            rating.setTotalRatings(totalRatings);
            rating.setRating1Count(rating1Count);
            rating.setRating2Count(rating2Count);
            rating.setRating3Count(rating3Count);
            rating.setRating4Count(rating4Count);
            rating.setRating5Count(rating5Count);

            departmentRatingRepository.save(rating);
            log.info("Recalculated rating for department: {}", departmentId);
        } catch (Exception e) {
            log.error("Error recalculating rating for department: {}", departmentId, e);
            throw e;
        }
    }

    private com.smartcityfix.feedback.dto.FeedbackResponse mapToFeedbackResponse(Feedback feedback) {
        List<FeedbackResponseDto> responses = feedbackResponseRepository.findByFeedbackId(feedback.getId())
                .stream()
                .map(this::mapToFeedbackResponseDto)
                .collect(Collectors.toList());

        return com.smartcityfix.feedback.dto.FeedbackResponse.builder()
                .id(feedback.getId())
                .complaintId(feedback.getComplaintId())
                .userId(feedback.getUserId())
                .departmentId(feedback.getDepartmentId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .status(feedback.getStatus())
                .anonymous(feedback.isAnonymous())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .responses(responses)
                .build();
    }

    private FeedbackResponseDto mapToFeedbackResponseDto(FeedbackResponse response) {
        return FeedbackResponseDto.builder()
                .id(response.getId())
                .feedbackId(response.getFeedbackId())
                .responderId(response.getResponderId())
                .response(response.getResponse())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }

    private DepartmentRatingDto mapToDepartmentRatingDto(DepartmentRating rating) {
        return DepartmentRatingDto.builder()
                .departmentId(rating.getDepartmentId())
                .averageRating(rating.getAverageRating())
                .totalRatings(rating.getTotalRatings())
                .rating1Count(rating.getRating1Count())
                .rating2Count(rating.getRating2Count())
                .rating3Count(rating.getRating3Count())
                .rating4Count(rating.getRating4Count())
                .rating5Count(rating.getRating5Count())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}