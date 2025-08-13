package com.smartcityfix.complaint.dto;

import com.smartcityfix.complaint.model.ComplaintStatus;
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
public class StatusHistoryDto {

    private UUID id;
    private ComplaintStatus oldStatus;
    private ComplaintStatus newStatus;
    private UUID changedBy;
    private String notes;
    private LocalDateTime timestamp;
}