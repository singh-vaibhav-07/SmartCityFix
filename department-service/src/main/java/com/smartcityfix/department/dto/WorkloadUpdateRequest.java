package com.smartcityfix.department.dto;

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
public class WorkloadUpdateRequest {

    @NotNull(message = "Department ID is required")
    private UUID departmentId;

    @NotNull(message = "Operation is required")
    private WorkloadOperation operation;

    public enum WorkloadOperation {
        INCREMENT,
        DECREMENT
    }
}