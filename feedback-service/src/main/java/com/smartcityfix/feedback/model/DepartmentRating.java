package com.smartcityfix.feedback.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "department_ratings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRating {

    @Id
    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "average_rating", nullable = false)
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_ratings", nullable = false)
    @Builder.Default
    private Integer totalRatings = 0;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "rating1_count", nullable = false)
    @Builder.Default
    private Integer rating1Count = 0;

    @Column(name = "rating2_count", nullable = false)
    @Builder.Default
    private Integer rating2Count = 0;

    @Column(name = "rating3_count", nullable = false)
    @Builder.Default
    private Integer rating3Count = 0;

    @Column(name = "rating4_count", nullable = false)
    @Builder.Default
    private Integer rating4Count = 0;

    @Column(name = "rating5_count", nullable = false)
    @Builder.Default
    private Integer rating5Count = 0;
}