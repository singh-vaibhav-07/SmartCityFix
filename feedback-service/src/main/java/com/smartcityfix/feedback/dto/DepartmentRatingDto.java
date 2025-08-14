package com.smartcityfix.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRatingDto {

    private UUID departmentId;
    private Double averageRating;
    private Integer totalRatings;
    private Integer rating1Count;
    private Integer rating2Count;
    private Integer rating3Count;
    private Integer rating4Count;
    private Integer rating5Count;
    private LocalDateTime updatedAt;
}