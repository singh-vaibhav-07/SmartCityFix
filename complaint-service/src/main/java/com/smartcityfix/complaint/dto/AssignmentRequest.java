package com.smartcityfix.complaint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotNull(message = "Assigned by is required")
    private UUID assignedBy;

    private String notes;
}