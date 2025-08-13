package com.smartcityfix.common.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ComplaintAssignedEvent extends BaseEvent {
    private UUID complaintId;
    private UUID departmentId;
    private String departmentName;

    public ComplaintAssignedEvent(UUID complaintId, UUID departmentId, String departmentName) {
        super("COMPLAINT_ASSIGNED");
        this.complaintId = complaintId;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }
}