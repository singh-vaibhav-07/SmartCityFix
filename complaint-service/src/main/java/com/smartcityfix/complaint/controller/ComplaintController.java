package com.smartcityfix.complaint.controller;

import com.smartcityfix.common.dto.ApiResponse;
import com.smartcityfix.complaint.dto.*;
import com.smartcityfix.complaint.model.ComplaintCategory;
import com.smartcityfix.complaint.model.ComplaintStatus;
import com.smartcityfix.complaint.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Complaint Management", description = "APIs for complaint creation, search, and lifecycle management")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @Operation(summary = "Create a new complaint", description = "Creates a new complaint with OPEN status")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(@Valid @RequestBody ComplaintRequest request) {
        log.info("Received request to create complaint: {}", request.getTitle());
        ComplaintResponse response = complaintService.createComplaint(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint by ID", description = "Returns complaint details for the given ID")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable UUID id) {
        log.info("Fetching complaint with id: {}", id);
        ComplaintResponse response = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Search complaints", description = "Search complaints with various filters")
    public ResponseEntity<ApiResponse<Page<ComplaintResponse>>> searchComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintCategory category,
            @RequestParam(required = false) UUID reportedBy,
            @RequestParam(required = false) UUID assignedTo,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        log.info("Searching complaints with filters: status={}, category={}, reportedBy={}, assignedTo={}",
                status, category, reportedBy, assignedTo);

        ComplaintSearchRequest request = ComplaintSearchRequest.builder()
                .status(status)
                .category(category)
                .reportedBy(reportedBy)
                .assignedTo(assignedTo)
                .page(page)
                .size(size)
                .build();

        Page<ComplaintResponse> response = complaintService.searchComplaints(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign complaint", description = "Assigns complaint to a department")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignComplaint(
            @PathVariable UUID id,
            @Valid @RequestBody AssignmentRequest request) {
        log.info("Assigning complaint {} to department {}", id, request.getDepartmentId());
        ComplaintResponse response = complaintService.assignComplaint(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint assigned successfully", response));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update complaint status", description = "Updates the status of a complaint")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        log.info("Updating status of complaint {} to {}", id, request.getStatus());
        ComplaintResponse response = complaintService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete complaint", description = "Deletes a complaint by ID")
    public ResponseEntity<ApiResponse<Void>> deleteComplaint(@PathVariable UUID id) {
        log.info("Deleting complaint with id: {}", id);
        complaintService.deleteComplaint(id);
        return ResponseEntity.ok(ApiResponse.successNoData("Complaint deleted successfully"));
    }
}