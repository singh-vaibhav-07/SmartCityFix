package com.smartcityfix.complaint.dto;

import com.smartcityfix.complaint.model.ComplaintCategory;
import com.smartcityfix.complaint.model.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintSearchRequest {

    private ComplaintStatus status;
    private ComplaintCategory category;
    private UUID reportedBy;
    private UUID assignedTo;
    private Integer page;
    private Integer size;
}