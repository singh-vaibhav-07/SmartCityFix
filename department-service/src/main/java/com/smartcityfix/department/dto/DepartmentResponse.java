package com.smartcityfix.department.dto;

import com.smartcityfix.department.model.ComplaintCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    private UUID id;
    private String name;
    private Set<ComplaintCategory> categories;
    private String zone;
    private String contactEmail;
    private String contactPhone;
    private String endpoint;
    private Integer capacity;
    private Integer currentWorkload;
    private LocationDto location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}