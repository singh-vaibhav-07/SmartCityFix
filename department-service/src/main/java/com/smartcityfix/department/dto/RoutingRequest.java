package com.smartcityfix.department.dto;

import com.smartcityfix.department.model.ComplaintCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingRequest {

    @NotNull(message = "Category is required")
    private ComplaintCategory category;

    @Valid
    private LocationDto location;

    private String zone;
}