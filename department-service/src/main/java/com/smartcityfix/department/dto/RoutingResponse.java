package com.smartcityfix.department.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingResponse {

    private UUID departmentId;
    private String name;
    private String endpoint;
    private String contactEmail;
    private String zone;
}