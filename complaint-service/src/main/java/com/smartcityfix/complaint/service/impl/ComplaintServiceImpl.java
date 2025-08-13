package com.smartcityfix.complaint.service.impl;

import com.smartcityfix.common.exception.ResourceNotFoundException;
import com.smartcityfix.complaint.dto.*;
import com.smartcityfix.complaint.exception.InvalidStatusTransitionException;
import com.smartcityfix.complaint.messaging.ComplaintEventPublisher;
import com.smartcityfix.complaint.model.*;
import com.smartcityfix.complaint.repository.ComplaintRepository;
import com.smartcityfix.complaint.service.ComplaintService;
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
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintEventPublisher eventPublisher;

    @Override
    @Transactional
    @CircuitBreaker(name = "complaintService", fallbackMethod = "createComplaintFallback")
    @Retry(name = "complaintService")
    public ComplaintResponse createComplaint(ComplaintRequest request) {
        log.info("Creating new complaint with title: {}", request.getTitle());

        try {
            Complaint complaint = Complaint.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .category(request.getCategory())
                    .priority(request.getPriority())
                    .status(ComplaintStatus.OPEN)
                    .location(mapToLocation(request.getLocation()))
                    .reportedBy(request.getReportedBy())
                    .build();

            // Add initial status history
            StatusHistory initialStatus = StatusHistory.builder()
                    .oldStatus(null)
                    .newStatus(ComplaintStatus.OPEN)
                    .changedBy(request.getReportedBy())
                    .notes("Complaint created")
                    .build();

            complaint.addStatusHistory(initialStatus);

            Complaint savedComplaint = complaintRepository.save(complaint);
            log.info("Complaint created successfully with id: {}", savedComplaint.getId());

            // Publish event
            eventPublisher.publishComplaintCreatedEvent(savedComplaint);

            return mapToComplaintResponse(savedComplaint);
        } catch (Exception e) {
            log.error("Error creating complaint", e);
            throw e;
        }
    }

    public ComplaintResponse createComplaintFallback(ComplaintRequest request, Exception e) {
        log.error("Fallback: Error creating complaint", e);
        throw new RuntimeException("Service is currently unavailable. Please try again later.");
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(UUID id) {
        log.info("Fetching complaint with id: {}", id);

        try {
            Complaint complaint = complaintRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

            log.info("Complaint found: {}", complaint.getId());
            return mapToComplaintResponse(complaint);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching complaint with id: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> searchComplaints(ComplaintSearchRequest request) {
        log.info("Searching complaints with filters: {}", request);

        try {
            int page = request.getPage() != null ? request.getPage() : 0;
            int size = request.getSize() != null ? request.getSize() : 10;

            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            log.debug("Executing repository query with status={}, category={}, reportedBy={}, assignedTo={}",
                    request.getStatus(), request.getCategory(), request.getReportedBy(), request.getAssignedTo());

            Page<Complaint> complaints = complaintRepository.findByFilters(
                    request.getStatus(),
                    request.getCategory(),
                    request.getReportedBy(),
                    request.getAssignedTo(),
                    pageable);

            log.info("Found {} complaints", complaints.getTotalElements());

            return complaints.map(this::mapToComplaintResponse);
        } catch (Exception e) {
            log.error("Error searching complaints", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ComplaintResponse assignComplaint(UUID id, AssignmentRequest request) {
        log.info("Assigning complaint {} to department {}", id, request.getDepartmentId());

        try {
            Complaint complaint = complaintRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

            // Check if complaint is in a valid state for assignment
            if (complaint.getStatus() != ComplaintStatus.OPEN) {
                throw new InvalidStatusTransitionException(
                        "Cannot assign complaint with status " + complaint.getStatus());
            }

            // Update complaint
            complaint.setAssignedTo(request.getDepartmentId());
            complaint.setStatus(ComplaintStatus.ASSIGNED);

            // Add status history
            StatusHistory statusHistory = StatusHistory.builder()
                    .oldStatus(ComplaintStatus.OPEN)
                    .newStatus(ComplaintStatus.ASSIGNED)
                    .changedBy(request.getAssignedBy())
                    .notes(request.getNotes())
                    .build();

            complaint.addStatusHistory(statusHistory);

            Complaint updatedComplaint = complaintRepository.save(complaint);
            log.info("Complaint {} assigned to department {}", id, request.getDepartmentId());

            // Publish event
            eventPublisher.publishComplaintAssignedEvent(updatedComplaint);

            return mapToComplaintResponse(updatedComplaint);
        } catch (ResourceNotFoundException | InvalidStatusTransitionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error assigning complaint", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ComplaintResponse updateStatus(UUID id, StatusUpdateRequest request) {
        log.info("Updating status of complaint {} to {}", id, request.getStatus());

        try {
            Complaint complaint = complaintRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));

            // Validate status transition
            validateStatusTransition(complaint.getStatus(), request.getStatus());

            ComplaintStatus oldStatus = complaint.getStatus();
            complaint.setStatus(request.getStatus());

            // Add status history
            StatusHistory statusHistory = StatusHistory.builder()
                    .oldStatus(oldStatus)
                    .newStatus(request.getStatus())
                    .changedBy(request.getUpdatedBy())
                    .notes(request.getNotes())
                    .build();

            complaint.addStatusHistory(statusHistory);

            Complaint updatedComplaint = complaintRepository.save(complaint);
            log.info("Complaint {} status updated to {}", id, request.getStatus());

            // Publish appropriate event based on new status
            if (request.getStatus() == ComplaintStatus.RESOLVED) {
                eventPublisher.publishComplaintResolvedEvent(
                        updatedComplaint, request.getUpdatedBy(), request.getNotes());
            } else {
                eventPublisher.publishComplaintStatusUpdatedEvent(
                        updatedComplaint, oldStatus.name(), request.getStatus().name());
            }

            return mapToComplaintResponse(updatedComplaint);
        } catch (ResourceNotFoundException | InvalidStatusTransitionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating complaint status", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteComplaint(UUID id) {
        log.info("Deleting complaint with id: {}", id);

        try {
            if (!complaintRepository.existsById(id)) {
                throw new ResourceNotFoundException("Complaint", "id", id);
            }

            complaintRepository.deleteById(id);
            log.info("Complaint deleted successfully: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting complaint with id: {}", id, e);
            throw e;
        }
    }

    private void validateStatusTransition(ComplaintStatus currentStatus, ComplaintStatus newStatus) {
        // Define valid transitions
        boolean isValid = switch (currentStatus) {
            case OPEN -> newStatus == ComplaintStatus.ASSIGNED;
            case ASSIGNED -> newStatus == ComplaintStatus.IN_PROGRESS || newStatus == ComplaintStatus.OPEN;
            case IN_PROGRESS -> newStatus == ComplaintStatus.RESOLVED || newStatus == ComplaintStatus.ASSIGNED;
            case RESOLVED -> newStatus == ComplaintStatus.CLOSED || newStatus == ComplaintStatus.IN_PROGRESS;
            case CLOSED -> false; // Closed is terminal state
        };

        if (!isValid) {
            throw new InvalidStatusTransitionException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private Location mapToLocation(LocationDto locationDto) {
        return Location.builder()
                .latitude(locationDto.getLatitude())
                .longitude(locationDto.getLongitude())
                .address(locationDto.getAddress())
                .build();
    }

    private LocationDto mapToLocationDto(Location location) {
        return LocationDto.builder()
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .address(location.getAddress())
                .build();
    }

    private StatusHistoryDto mapToStatusHistoryDto(StatusHistory history) {
        return StatusHistoryDto.builder()
                .id(history.getId())
                .oldStatus(history.getOldStatus())
                .newStatus(history.getNewStatus())
                .changedBy(history.getChangedBy())
                .notes(history.getNotes())
                .timestamp(history.getTimestamp())
                .build();
    }

    private ComplaintResponse mapToComplaintResponse(Complaint complaint) {
        List<StatusHistoryDto> historyDtos = complaint.getHistory().stream()
                .map(this::mapToStatusHistoryDto)
                .collect(Collectors.toList());

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .category(complaint.getCategory())
                .priority(complaint.getPriority())
                .status(complaint.getStatus())
                .location(mapToLocationDto(complaint.getLocation()))
                .reportedBy(complaint.getReportedBy())
                .assignedTo(complaint.getAssignedTo())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .history(historyDtos)
                .build();
    }
}